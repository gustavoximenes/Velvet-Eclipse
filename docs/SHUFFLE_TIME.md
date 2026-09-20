# Shuffle Time — 0.6.16

Uma vitória causada pelo All-Out Attack concede uma ativação de Shuffle Time a cada jogador que continua participando da batalha. A recompensa só é anunciada após o término da sequência de vitória. All-Out Attack não letal, ataque normal e vitória comum não concedem a ativação.

O indicador azul no canto inferior direito informa a quantidade acumulada e a tecla configurada, **P** por padrão. Durante as batalhas ele fica oculto. Pressione P fora de combate para abrir a animação Shuffle Time e escolher uma entre 1 e 5 cartas. Use as setas, o mouse ou as teclas 1–5 e confirme com Enter; clicar novamente na carta selecionada também confirma. Esc fecha sem gastar a ativação. As cartas de cada oferta são persistidas: reabrir, morrer ou reconectar não as sorteia novamente.

- **Persona:** mostra o modelo 3D do mod e concede a Persona escolhida com nível correspondente ao andar (limitado a 99). Apenas espécies com nível-base compatível podem aparecer. Prioriza Personas ainda não possuídas; duplicatas fortalecem a mesma Persona sem apagar habilidades, nível ou EXP existentes. São utilizadas as sete Personas já implementadas.
- **Cups / Copas:** recupera de 20% a 60% do HP máximo do mod, conforme o andar, sem ultrapassar o máximo.
- **Coins:** concede yen: `100 + 75 × andar`.
- **Wands:** concede `60 + 20 × andar` EXP ao jogador e à Persona equipada.
- **Swords:** concede um Skill Card compatível com o andar. As habilidades básicas usam o nível mínimo do aprendizado existente. Mazio requer nível 8; Zionga 20; Mazionga 32; Ziodyne 50; Maziodyne 65.

Cada oferta possui tipos diferentes, e a seleção consome uma ativação inteira. Fora do Tartarus, a recompensa usa o andar 1. A moeda do menu Tab agora é yen persistente do próprio mod, sem relação com a quantidade de esmeraldas. Saves antigos começam com ¥0, preservando os demais dados.

## Skill Cards e criativo

Os 19 Skill Cards das habilidades atuais estão na aba Velvet Eclipse, com os 24 blocos do Tartarus, incluindo os portais. Segure o cartão e use o botão direito; selecione uma Persona possuída e confirme para ensinar a habilidade. Uma unidade é consumida somente após sucesso. Uma Persona que já conhece a habilidade não consome o cartão. Todos os Skill Cards usam a mesma regra de aprendizagem, inclusive os obtidos pelo criativo.

Inventário cheio preserva a oferta de Swords. A seleção e o recebimento são validados no servidor, incluindo identificação da oferta, propriedade da Persona e presença do cartão na mão. A tecla P pode ser reconfigurada em Controles.

## Correções

O portal de retorno dos andares fica na parede norte da sala inicial, que não possui conexão com corredores, com uma parede de segurança atrás. Andares já gerados são corrigidos quando visitados, sem refazer o mapa ou suas Shadows.

A prévia do Igor gira o modelo diretamente. O GeoEntityRenderer do GeckoLib 4.8.4 usa rotação zero para entidades que não herdam LivingEntity e ignorava o yaw atribuído ao Igor. A correção atua apenas na prévia do menu.

## Verificação

Auditorias focadas em Shuffle Time e portais: limites de nível, persistência, migração de yen, resgate único, inventário cheio, aprendizagem, modelos de itens e caminhos entre salas. Cartas e animação de entrada também conferidas em prévias offline. Não houve execução no Minecraft ou teste com dois clientes, conforme a preferência do usuário. Instalar 0.6.16 no servidor e nos clientes; o protocolo do menu agora é 3 e há um canal dedicado ao Shuffle Time.
