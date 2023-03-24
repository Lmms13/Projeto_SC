Grupo 31
Luis Santos 56341
Pedro Pinto 56369
Daniel Marques 56379

----------------EXECUCAO----------------

O ficheiro .jar do servidor executa-se com o comando:
java -jar TintolmarketServer.jar <porto>

O ficheiro .jar do cliente executa-se com o comando:
java -jar Tintolmarket.jar <IP/hostname>:<porto> <userID> <password>

Tendo em conta as bases de dados criadas durante a realizacao do projeto,
seguem-se alguns exemplos de uso para o servidor e para clientes:

java -jar TintolmarketServer.jar 12346

java -jar Tintolmarket.jar localhost:12346 daguel pedro1234
java -jar Tintolmarket.jar localhost:12346 pedro pass123
java -jar Tintolmarket.jar localhost:12346 pinto pass124
java -jar Tintolmarket.jar localhost:12346 aulelas 56363


----------------DETALHES DA IMPLEMENTACAO----------------

O programa nao cria a arvore de diretorias necessaria para a sua execucao,
principalmente no que toca aos ficheiros que servem de bases de dados. Deste
modo, assume-se que os ficheiros .jar sao sempre executados no contexto em que
foram submetidos, ou seja:


-bin
-src
	-client
		-images
			-(clientes* - estas diretorias sao criadas pelo programa)
				-(imagens)
		-Tintomarket.java
	-domain
		-catalogs
			-(catalogos)
		-(classes do dominio)
	-server
		-files
			-(bases de dados) <----------IMPORTANTE
		-images
			-(imagens)		  <----------IMPORTANTE
		-TintolmarketServer.java
-README
-Tintolmarket.jar
-TintolmarketServer.jar


O path para as diferentes bases de dados e importante e estes ficheiros 
e diretorias nao sao criados se nao forem encontrados, visto que o grupo
nao considerou esse detalhe relevante, assumindo-se que o codigo foi escrito
com base num sistema de ficheiros cuja estrutura nao e modificada. 

Se os professores desejarem usar dados novos em vez dos dados ja existentes 
nos nossos exemplos de bases de dados, agradecemos que apagem ou editem 
apenas o conteudo dos ficheiros .txt, e nao os ficheiros em si, de modo a 
nao comprometer o funcionamento do programa, que foi concretizado para ser 
executado no contexto em que foi entregue.