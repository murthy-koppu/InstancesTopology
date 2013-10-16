InstancesTopology
=================

InstancesTopology

Create AwsCredentials.properties file with properties: secretKey, accessKey of Amazon AWS Console Credentials.
Create directory hierarchy AWSInstanceKeyPairs / InstancePrivateKeyFileMap, create file KeyPairInstanceIdsMap.properties
Put property name PEM key with values as instanceId,userId(seperated by comma) and each instance is seperated by ;

Create config.properties and put the propeties:

request.region="Indicating from which region instances needs to be loaded"
instanceKeyPairs.directory="Directory from where PEM Key files are located"

Include AwsCredentials.properties, AWSInstanceKeyPairs,config.properties in  InstancesTopology/src/main/resources

