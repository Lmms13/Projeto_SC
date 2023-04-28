Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379

----------------EXECUCAO----------------

O ficheiro .jar do servidor executa-se com o comando:
java -jar TintolmarketServer <port> <password-cifra> <keystore> <password-keystore>

O ficheiro .jar do cliente executa-se com o comando:
java -jar Tintolmarket <IP/hostname>:<porto> <truststore> <keystore> <password-keystore> <userID>

Tendo em conta as bases de dados e keystores criadas durante
o desenvolvimento do projeto, para assegurar o funcionamento
do programa com as keystores e certificados incluidos no
ficheiro zip submetido, os seguintes valores sao aceites:

SERVIDOR:
password-cifra: aulelas.cifra
keystore: server.keystore 
password-keystore: aulelas.keystore
-----------------------------------

CLIENTE:
truststore: client.truststore
keystore: <userID>.keystore (aceita qualquer um dos 5 userIDs listados)
password-keystore: aulelas.keystore (o grupo reconhece que as passwords devem 
									 ser diferentes, mas usou-se sempre a 
									 mesma para facilitar o processo de 
									 criacao e verificacao das keystores)
userID: pinto
		rodolfo
		pedro
		daguel
		aulelas
		
As passwords das chaves dos utilizadores estao hardcoded como "<userID>.key"
------------------------------------
Desse modo, seguem-se alguns comandos possiveis para executar o programa:

java -jar TintolmarketServer.jar 12346 aulelas.cifra server.keystore aulelas.keystore

java -jar Tintolmarket.jar localhost:12346 client.truststore pinto.keystore aulelas.keystore pinto
java -jar Tintolmarket.jar localhost:12346 client.truststore rodolfo.keystore aulelas.keystore rodolfo
java -jar Tintolmarket.jar localhost:12346 client.truststore pedro.keystore aulelas.keystore pedro
java -jar Tintolmarket.jar localhost:12346 client.truststore daguel.keystore aulelas.keystore daguel
java -jar Tintolmarket.jar localhost:12346 client.truststore aulelas.keystore aulelas.keystore aulelas

----------------DETALHES DA IMPLEMENTACAO----------------

O programa nao cria a arvore de diretorias necessaria para a sua execucao,
principalmente no que toca aos ficheiros que servem de bases de dados e 
keystores. Deste modo, assume-se que os ficheiros .jar sao sempre executados 
no contexto em que foram submetidos, ou seja:


-bin
-src
	-client
		-images
			-(clientes* - estas diretorias sao criadas pelo programa)
				-(imagens)
		-files
			-(certificados e keystores*)
		-Tintomarket.java
	-domain
		-catalogs
			-(catalogos)
		-(classes do dominio)
	-server
		-blockchain
			-Block.java
			-BlockchainHandler.java
			-(blocks da blockchain*)
			-server.secret(keystore com chave secreta para concretizar o MAC)
		-files
			-(bases de dados)
			-(MACs das bases de dados)
			-server.keystore
			-(certificados dos clientes)
		-images
			-(imagens)
		-TintolmarketServer.java
-README
-Tintolmarket.jar
-TintolmarketServer.jar


O path para as diferentes bases de dados e importante e estes ficheiros 
e diretorias nao sao criados se nao forem encontrados, visto que o grupo
nao considerou esse detalhe relevante, assumindo-se que o codigo foi escrito
com base num sistema de ficheiros cuja estrutura nao e modificada. 

Agradecemos, tambem, que os ficheiros de bases de dados nao sejam apagados.
Caso o conteudo desejado para testagem seja diferente, pedimos aos professores
que apenas alterem o conteudo dos ficheiros e que nao apaguem os ficheiros em 
si nem as diretorias em que estes estao presentes, de modo a nao comprometer o 
funcionamento do programa, que foi concretizado para ser executado no contexto 
em que foi entregue.