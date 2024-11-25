# Introdução

Esta é a minha resposta ao teste Cloudwalk EngSec, que irá descrever o meu processo de pensamento, bem como acompanhar quaisquer evidências que achei de interesse, e os meus pensamentos pessoais sobre como analisar o conjunto de dados que me foi dado (e inserido neste repo)

## Primeiros passos

Antes de qualquer coisa, o que fiz primeiro, dado o conjunto de dados CSV, foi instalar o Grafana e usá-lo para interpretar o CSV com o plugin infinity.

![Evidência 1](/photos/1.png?raw=true “Evidência 1”)

Com os dados agora carregados no Grafana, a primeira coisa que fiz foi uma transformação de dados, obtendo uma contagem de todos os IPs de clientes presentes no conjunto de dados e classificando-os pelo seu IP, e foi aqui que encontrei o meu primeiro risco provável

### 1. Actividades de IP e URLs invulgares

![Evidência 2](/photos/2.png?raw=true “Evidência 2”)

Muitos IPs só acederam ao servidor uma vez, mas alguns outliers acederam ao servidor mais de 100 vezes, com o IP **53.153.77.110** acessando ao servidor **156 vezes**

Embora isto, por si só, possa não ser certamente um indicador de um incidente de segurança, aponta para atividades anômalas

![Evidência 3](/photos/3.png?raw=true “Evidência 3”)

Os acessos parecem não seguir um padrão sobre “quando” acessam ao servidor, mas têm algo em comum, todos acessam ao mesmo endpoint (/write/toward/story) exceto alguns pedidos que são suspeitos

Pedido 1 = /../../../windows/win.ini, tentando acessao ao sistema de arquivos do servidor
Pedidos 2, 3 e 4 = ```/<marquee><img src=1 onerror=alert(1)></marquee>, /“;!--”<XSS>=&{()}, /%00%01%02%03%04%05%06%07```, tentando inserir scripts/entradas não higienizadas no servidor

Estes tipos de pedidos repetem-se mais do que uma vez, tudo isto aponta para um malfeitor

![Evidência 4](/photos/4.png?raw=true “Evidência 4”)

Depois, continuei a verificar estes caminhos, quantas vezes aparecem, contando estes caminhos e ordenando-os por cada tipo de pedido

![Evidência 5](/fotos/5.png?raw=true “Evidência 5”)

Podemos ver que há muitas tentativas do gênero, e provenientes de diferentes IPs

![Evidência 6](/fotos/6.png?raw=true “Evidência 6”)

Outra coisa que também verifiquei depois, foi o tamanho em bytes do pedido, com certeza, dando-me mais provas de tentativas de acesso ao servidor e de exfiltração de dados

![Evidência 9](/photos/9.png?raw=true “Evidência 9”)

### Minha solução

Para resolver este problema, é necessário reforçar a segurança do servidor e da aplicação, o que inclui a validação da entrada, como em Java:

```
File file = new File(BASE_DIRECTORY, userInput);
if (file.getCanonicalPath().startsWith(BASE_DIRECTORY)) {
    // processar arquivo
}
```

e usar ferramentas SAST para garantir que o aplicativo do servidor siga os padrões de segurança

um WAF (Web Application Firewall) também pode ser usado para bloquear/desacelerar o tráfego anômalo, e um IDS/IPS também pode ser crucial para identificar e impedir quaisquer acessos anômalos ao servidor

### 2. Monitorização geográfica

Em seguida, passei a verificar o sinalizador ClientCountry, contando cada vez que um país aparece, tentando identificar se há um padrão entre os atacantes e os países

Embora eu não tenha encontrado nada de substancial, encontrei algo:

![Evidência 7](/photos/7.png?raw=true “Evidência 7”)

A maioria destas regiões de acesso são comuns, mas cn, China, é considerado um país de risco, e o seu bloqueio deve ser considerado

A Índia também pode ser considerada um país de risco, mas é necessário mais contexto para fazer uma escolha

### Minha solução

Para bloquear a China (e qualquer outro país de risco) de acessar o servidor, um WAF (Web Application Firewall) pode ser usado, como o da Cloudflare:

```
ip.geoip.country eq “CN”
```

### 3. Portos

Em seguida, passei a verificar as portas, um vetor comum de ataque

Fazendo a mesma análise de contagem e resumo das portas utilizadas, pude constatar que vários acessos utilizaram portas diferentes, mas uma delas se destacou para mim, a porta **0**

![Evidência 8](/photos/8.png?raw=true “Evidência 8”)

Embora seja necessária uma análise e um contexto mais profundos para determinar quais portas são de fato necessárias para o funcionamento adequado do servidor, 0 não é uma delas

### Minha solução

Auditar e restringir o uso de portas

Os firewalls devem ser usados para bloquear qualquer porta que não seja essencial para o funcionamento do servidor, e ferramentas como o nmap devem ser usadas para verificar se há portas abertas e vulneráveis

# Conclusão

Fiz a minha devida diligência com as ferramentas de que disponho e com os conhecimentos que tenho; embora possa ter-me escapado alguma coisa, creio que obtive a maioria dos eventos que poderiam ser registados como incidentes

As minhas considerações finais são as seguintes:

- As ferramentas de análise de registos, como o Grafana, são extremamente necessárias em casos como este. Com 0 pistas, encontrei algumas vulnerabilidades que, de outra forma, levariam séculos a verificar manualmente
- As ferramentas de automatização, como os sistemas SOAR, os fornecedores WAF e os SATS, podem ser utilizadas para ajudar na prevenção de ciberataques
- A cibersegurança deve fazer sempre parte do ciclo de desenvolvimento (mais uma vez, o SATS pode ser utilizado)
