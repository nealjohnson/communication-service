
Reference:
https://medium.com/@sun30nil/how-to-secure-secrets-and-passwords-in-springboot-90c952961d9

Encrypt values using following command

java -cp ~/.m2/repository/org/jasypt/jasypt/1.9.2/jasypt-1.9.2.jar org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="Topsecret@123" password=dev-env-secret algorithm=PBEWITHMD5ANDDES