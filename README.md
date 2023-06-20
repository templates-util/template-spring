# Projeto Base Spring Boot

Este é o projeto base para aplicativos desenvolvidos usando o Spring Boot. Ele fornece uma estrutura robusta e flexível para o desenvolvimento de aplicações web empresariais e microserviços.

## Configuração Inicial

Para configurar o projeto, precisamos inicializar os parâmetros corretamente. A configuração de permissões é lida a partir do arquivo `roles.properties`. Isso cria todas as permissões padrão necessárias para o sistema.

## Dependências

Este projeto usa o Maven como gerenciador de dependências. Portanto, todas as dependências necessárias estão listadas no arquivo `pom.xml`.

## Como executar

Para executar este projeto, você precisa ter o Java e o Maven instalados em seu ambiente. Depois disso, você pode clonar o repositório e executar o comando `mvn spring-boot:run` na raiz do projeto.

## Jenkins

Para utilizar a integração contínua com Jenkins, é necessário ter o Jenkins e o GitLab configurados corretamente. Note que apenas a presença do `Jenkinsfile` no projeto não garante a integração contínua.

Após a configuração do Jenkins e GitLab, é necessário renomear os comandos dentro do `Dockerfile` que está localizado na mesma pasta do `pom.xml`.

## Contribuição

Sinta-se à vontade para contribuir com este projeto. Se você encontrar um bug ou tiver uma sugestão de melhoria, por favor, abra uma issue ou um pull request.

## Licença

Este projeto está licenciado sob os termos da licença MIT.

Espero que este projeto base seja útil para você começar a desenvolver seus projetos usando o Spring Boot. Por favor, não hesite em contribuir ou relatar quaisquer problemas que você encontrar.
