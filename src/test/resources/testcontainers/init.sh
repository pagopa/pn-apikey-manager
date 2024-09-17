
echo " - Create pn-apikey-manager TABLES"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-aggregates \
    --attribute-definitions \
        AttributeName=aggregateId,AttributeType=S \
        AttributeName=pageable,AttributeType=S \
        AttributeName=name,AttributeType=S \
    --key-schema \
        AttributeName=aggregateId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --stream-specification \
        StreamEnabled=true,StreamViewType=NEW_IMAGE \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"aggregations-aggregateName-index\",
            \"KeySchema\": [{\"AttributeName\":\"pageable\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"name\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"


aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-paAggregations \
    --attribute-definitions \
        AttributeName=x-pagopa-pn-cx-id,AttributeType=S \
        AttributeName=pageable,AttributeType=S \
        AttributeName=paName,AttributeType=S \
        AttributeName=aggregateId,AttributeType=S \
    --key-schema \
        AttributeName=x-pagopa-pn-cx-id,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"pageable-paName-index\",
            \"KeySchema\": [{\"AttributeName\":\"pageable\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"paName\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        },
        {
            \"IndexName\": \"paAggregations-aggregateId-index\",
            \"KeySchema\": [{\"AttributeName\":\"aggregateId\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"x-pagopa-pn-cx-id\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-apiKey \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
        AttributeName=x-pagopa-pn-cx-id,AttributeType=S \
        AttributeName=lastUpdate,AttributeType=S \
        AttributeName=virtualKey,AttributeType=S \
        AttributeName=x-pagopa-pn-uid,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    "[
        {
            \"IndexName\": \"paId-lastUpdate-index\",
            \"KeySchema\": [{\"AttributeName\":\"x-pagopa-pn-cx-id\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"lastUpdate\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        },
        {
            \"IndexName\": \"virtualKey-id-index\",
            \"KeySchema\": [{\"AttributeName\":\"virtualKey\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"id\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        },
        {
            \"IndexName\": \"uid-cxId-index\",
            \"KeySchema\": [{\"AttributeName\":\"x-pagopa-pn-uid\",\"KeyType\":\"HASH\"},
                            {\"AttributeName\":\"x-pagopa-pn-cx-id\",\"KeyType\":\"RANGE\"}],
            \"Projection\":{
                \"ProjectionType\":\"ALL\"
            },
            \"ProvisionedThroughput\": {
                \"ReadCapacityUnits\": 10,
                \"WriteCapacityUnits\": 5
            }
        }
    ]"

echo "### CREATE PUBLIC KEY TABLE ###"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-publicKey \
    --attribute-definitions \
        AttributeName=kid,AttributeType=S \
        AttributeName=cxId,AttributeType=S \
        AttributeName=status,AttributeType=S \
        AttributeName=createdAt,AttributeType=S \
    --key-schema \
        AttributeName=kid,KeyType=HASH \
        AttributeName=cxId,KeyType=RANGE \
    --provisioned-throughput ReadCapacityUnits=10,WriteCapacityUnits=5 \
    --global-secondary-indexes \
    '[
        {
            "IndexName": "cxId-status-index",
            "KeySchema": [
                {"AttributeName":"cxId","KeyType":"HASH"},
                {"AttributeName":"status","KeyType":"RANGE"}
            ],
            "Projection": {"ProjectionType":"ALL"},
            "ProvisionedThroughput": {"ReadCapacityUnits": 10, "WriteCapacityUnits": 5}
        },
        {
            "IndexName": "cxId-createdAt-index",
            "KeySchema": [
                {"AttributeName":"cxId","KeyType":"HASH"},
                {"AttributeName":"createdAt","KeyType":"RANGE"}
            ],
            "Projection": {"ProjectionType":"ALL"},
            "ProvisionedThroughput": {"ReadCapacityUnits": 10, "WriteCapacityUnits": 5}
        }
    ]'

echo "Initialization terminated"