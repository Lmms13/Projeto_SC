Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379

O ficheiro .jar do servidor executa-se com o comando:
java -jar TintolmarketServer.jar <porto>

O ficheiro .jar do cliente executa-se com o comando:
java -jar Tintolmarket.jar <IP/hostname>:<porto> <userID> <password>

Tendo em conta as bases de dados criadas durante a realizacao do projeto,
seguem-se alguns exemplos de uso para o servidor e para clientes:

java -jar TintolmarketServer.jar 12346

java -jar Tintolmarket.jar hostname:12346 daguel pedro1234
java -jar Tintolmarket.jar hostname:12346 pedro pass123
java -jar Tintolmarket.jar hostname:12346 pinto pass124
java -jar Tintolmarket.jar hostname:12346 aulelas 56363