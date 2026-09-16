# Velvet Eclipse 0.6.12 — Fusão e Igor

Esta versão integra o Igor sentado na Velvet Room e abre a tela de fusão ao conversar com ele ou ao chegar à sala. A tela tem os modos Fusão de duas Personas, Busca por resultado e Personas possuídas. A seleção de duas Personas abre a ficha reutilizada do menu, com atributos, afinidades, habilidades e modelo 3D do resultado; a confirmação do servidor é de uso único e só então consome as duas fontes.

O nível usado no limite é o nível próprio do jogador do mod. Ele começa no nível 1, recebe experiência nas vitórias e é persistido junto ao roster. Pré-visualizações, cancelamentos, expiração, alterações no roster e confirmações repetidas não consomem Personas. Se o resultado já existir, seu nível, experiência e habilidades são preservados.

O modelo aprovado do Igor foi exportado sem regeneração: 35 bones, 102 cuboids, textura Box UV 512×512 e idle misc.idle. A textura exportada é byte-idêntica ao draft aprovado, inclusive a correção dos olhos.

JAR: `velvet-eclipse-0.6.12-fusion.jar`

SHA-256: `398EC49186F3834780979B1125A3460FB393DEF538A2D6A2FB8558E1D00C91B7`

O usuário dispensou teste dentro do Minecraft. A verificação offline confirmou os recursos do Igor, os novos canais/classes de fusão e a presença da dimensão/porta existentes. O Gradle não completou a fase final porque o sandbox bloqueou a leitura de JARs de dependência locais; por isso o JAR foi montado a partir dos `.class` gerados pelo compilador Java e dos recursos processados.
