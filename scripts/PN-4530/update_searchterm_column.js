const { DynamoDBClient, ScanCommand, UpdateTableCommand, UpdateItemCommand } = require("@aws-sdk/client-dynamodb");
const { marshall } = require("@aws-sdk/util-dynamodb");
const { fromIni } = require("@aws-sdk/credential-provider-ini");
const { STSClient, AssumeRoleCommand } = require("@aws-sdk/client-sts");

const arguments = process.argv;

if (arguments.length <= 2) {
  console.error("Specify AWS profile as argument");
  process.exit(1);
}

const awsProfile = arguments[2]
const roleArn = arguments[3]

console.log("Using profile " + awsProfile);

function awsProfileConfig() {
  if(awsProfile.indexOf('sso_')>=0){
    return { 
      region: "eu-south-1", 
      credentials: fromIni({ 
        profile: awsProfile,
      })
    }
  }else{
    return { 
      region: "eu-south-1", 
      credentials: fromIni({ 
        profile: awsProfile,
        roleAssumer: async (sourceCredentials, params) => {
          const stsClient = new STSClient({ credentials: sourceCredentials });
          const command = new AssumeRoleCommand({
            RoleArn: roleArn,
            RoleSessionName: "session1"
          });
          const response = await stsClient.send(command);
          return {
            accessKeyId: response.Credentials.AccessKeyId,
            secretAccessKey: response.Credentials.SecretAccessKey,
            sessionToken: response.Credentials.SessionToken,
            expiration: response.Credentials.Expiration
          };
        }
      })
    }
  }
}

const dynamoDBClient = new DynamoDBClient(awsProfileConfig());

const TABLE_NAME = 'pn-aggregates';
const SCAN_LIMIT = 10; // max 2000
DELAY_MS = 1000; //1 second

var index = 1;

const aggregateId = new Map([
]);

const scanParams = {
  TableName: TABLE_NAME,
  Limit: SCAN_LIMIT
};


function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}


async function scanTable(params, callback) {
  const scanCommand = new ScanCommand(params);
  dynamoDBClient.send(scanCommand, function(err, data) {
    if (err) {
      callback(err, null);
    } else {
      setTimeout(function() {
        callback(null, data);

        if (typeof data.LastEvaluatedKey !== 'undefined') {
          params.ExclusiveStartKey = data.LastEvaluatedKey;
          //console.log("Params with LEK: ", params)
          scanTable(params, callback);
        }
      }, DELAY_MS);
    }
  });
}


async function main(){

  console.log('start update item');

  scanTable(scanParams, function(err, data) {
    if (err) {
      console.log(err);
    } else {

      console.log("Scanned items: ", data.Items.length);

      data.Items.forEach(async function (item) {
        const key = item.aggregateId.S;
        const value = item.name.S.toLowerCase();
        console.log("Id: ", key, "at Index: ", index++);
        let updateExpression = "SET #searchterm = :lowercaseName";
        let updateParams = {
          TableName: TABLE_NAME,
          Key: {
              "aggregateId": { S: key }
          },
          UpdateExpression: updateExpression,
          ExpressionAttributeNames: {
            "#searchterm": 'searchterm'
          },
          ExpressionAttributeValues: marshall(
            {
              ":lowercaseName": value
            }
          ) 

        };

        try {
          const updateItemCommand = new UpdateItemCommand(updateParams);
          await dynamoDBClient.send(updateItemCommand);
          console.log("Aggiornato elemento con key: ", key);
        } catch (error) {
          console.log(error);
          console.error("Errore nell'aggiornamento dell'elemento con key: ", key);
        }
      }
    );
    }
  })
      
}

main();



