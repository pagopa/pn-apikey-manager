## Onboard institutions DynamoDB Refactor

Installare dipendenze node:
`npm install` 

Eseguire il comando:
`node update_searchterm_column.js <aws-profile> <role_arn>`

Dove `<aws-profile>` è il profilo dell'account AWS.

`<role_arn>` è necessario solo per utenti che necessitano dell'esecuzione di assume role

Note:

1) lo script esegue un aggiornamento massivo della tabella `pn-aggregates` quindi si raccomanda di eseguire un backup prima della sua esecuzione.

2) lo script viene eseguito sempre nella region `eu-south-1` 

