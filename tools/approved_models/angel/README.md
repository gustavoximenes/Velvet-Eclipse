> Integrada como Angel03 na versão 0.6.57, em 01/11/2026, após aprovação. O texto abaixo registra a auditoria original. Veja `../../../docs/angel-entry-0.6.57.md` para a integração.

# Angel — auditoria de modelagem 03

31/10/2026. **Modelo editável para revisão; não integrado ao mod.**

Esta auditoria entrega a modelagem real, textura, animações e expressões faciais para corrigirmos o resultado visual até a aprovação. As imagens e vídeos são renders offline dos arquivos entregues, com iluminação simples. Não são capturas dentro do Minecraft, nem imagens geradas substituindo o modelo.

## Correções — revisão 03

A revisão 02 mantinha contato entre peças centrais, mas os segmentos usavam orientações independentes, produzindo torção aparente nos joelhos e cotovelos. As juntas não foram consideradas corretas apenas por terem caixas sobrepostas.

- Braços e pernas passaram a compartilhar um eixo anatômico de dobra entre seus dois segmentos. No ataque físico, o plano da dobra acompanha a projeção da perna, impedindo a inversão do joelho.
- As peças de união de joelhos, cotovelos e tornozelos acompanham metade da rotação relativa dos segmentos. Foram retiradas as pequenas peças chanfradas empilhadas dos joelhos.
- Coxas e panturrilhas usam poucos contornos largos em cuboides rotacionados, com volume contínuo e contato nas extremidades. Os pés menores acompanham a orientação da canela em vez de manter uma orientação independente no mundo.
- A corrente ganhou fixação física na gola, espaçamento revisado e orientação dos elos pela tangente média da curva. A checagem distingue metal em contato de elos entrelaçados: elos reais podem estar conectados pela abertura sem suas superfícies se atravessarem.

357 cuboides nativos, 79 ossos, Box UV e atlas 1024. Rosto, cabelo, venda, asas e quatro expressões mantidos. Estudo isolado: nenhuma integração ou geração de JAR.

`connectivity.py` verifica 343 poses dos sete clipes: o corpo, roupa, cabelo, asas e fixação devem formar um componente contínuo; todos os pares de elos vizinhos devem estar entrelaçados e o primeiro deve alcançar a fixação. `verify_model.py` mantém a checagem das juntas, UVs, hierarquia, rotações e afastamento entre membros/cabelo. Esses testes amostrados não substituem a inspeção visual nem constituem validação dentro do Minecraft.

`draft/angel-joints-03.gif` e `.mp4` aproximam as pernas em duas câmeras durante repouso, físico e magia (10,4 s). `draft/angel-polish-comparison-03.png` compara a revisão 02 e a 03 na mesma câmera e escala.

## Prévia no menu de Personas

`AngelMenuPreview.java` chama o **PersonaSheetDesign de produção** e o catálogo real da Angel: nível 4, Justice, atributos 4/5/3/4/3, Garu e Sukukaja, afinidades e próxima habilidade. Usa as fontes do projeto e idioma inglês padrão. Nenhuma regra do catálogo foi alterada.

`preview_menu.py` compõe a geometria editável, atlas e idle novos entre o fundo e primeiro plano desse menu. Propõe enquadramento para as asas dentro do viewport real (396, 8, 495, 444 na tela lógica 900×500), com escala fixa no idle. É uma **prévia offline de composição**, ainda não um perfil de render integrado. A iluminação é ilustrativa. O painel de atributos sobrepõe parte inferior do corpo, como no menu atual; a galeria oferece a mesma vista sem painéis para inspecionar as pernas.

Arquivos: `draft/angel-menu-03.png`, `.gif`, `.mp4`, `angel-menu-model-only-03.png`, `menu-catalogue.json` e `menu-validation.json`. Os 72 quadros do menu têm enquadramento verificado; o ciclo dura 4,8 s. Nenhum recurso de produção, menu ou JAR foi substituído.

## Referências e direção visual

Referência principal: [captura do compêndio e retrato de Persona 3 Reload](https://samurai-gamers.com/persona-3-reload/p3re-angel-persona-guide/), preservados em `references/`. A página contém um aviso editorial sobre dados do guia; a modelagem usa as **imagens de Reload**, sem importar seus dados de gameplay.

Também foi inspecionado o [compêndio em vídeo, 03:50,5–03:52,9](https://www.youtube.com/watch?v=s-Ya33_6Wfs&t=230s). O trecho mostra a postura inicial e uma ação com giro completo, braços acima da cabeça e asas recolhendo/abrindo. Ele não identifica a ação como ataque físico ou mágico. Não sustenta uma afirmação de fidelidade exata dos ataques ou do período de idle.

Cinco características preservadas na adaptação:

1. Figura feminina adulta e esguia, flutuando, com mãos erguidas e uma perna flexionada.
2. Venda azul escura opaca, rosto claro oval e boca pequena.
3. Cabelo loiro curto com mecha lateral, em volumes próximos à cabeça.
4. Asas brancas largas, com penas sobrepostas e articulações independentes.
5. Faixas escuras do figurino, corrente facetada e fitas atrás da cabeça.

As superfícies ocultas e os detalhes pequenos são adaptações autorais para cuboides. Nenhuma malha ou textura de personagem foi extraída do jogo.

## Rosto e expressões

A **Elizabeth revisão 05** do projeto fornece o método de contorno oval: bochechas cheias, mandíbula em facetas progressivas, queixo curto, nariz contínuo e detalhes rasos. A Nekomata 02 foi consultada para a separação entre cabeça, cabelo e controles faciais. O resultado foi adaptado ao rosto e à venda da Angel.

A venda permanece opaca em todas as expressões. Não foram inventados olhos visíveis, íris, piscadas ou sobrancelhas sobre o tecido. A atuação usa lábios separados, cantos da boca, mandíbula e orientação da cabeça:

| Expressão | Movimento proposto |
| --- | --- |
| Repouso | Boca discreta e cantos suaves, cabeça estável |
| Concentração | Lábios mais contidos e queixo levemente abaixado |
| Esforço | Abertura moderada da boca, mandíbula e cabeça acompanhando a ação |
| Reação a dano | Boca entreaberta e recuo/inclinação assimétrica da cabeça |

Essas quatro expressões são propostas autorais para o mod. O vídeo de referência não comprova esse conjunto facial original. O painel oferece close animado e prancha com a mesma câmera/escala para revisão.

## Animações

| Clipe | Duração | Evidência / adaptação |
| --- | --- | --- |
| `animation.angel.ground_idle` | 4,8 s, loop | Postura da referência; respiração, flutuação e período das asas propostos |
| `animation.angel.physical_attack` | 2,6 s | Chute curto, com os braços erguidos; **coreografia provisória**, sem referência de combate confirmada |
| `animation.angel.magic_attack` | 3,0 s | Giro e movimento de asas baseados na ação observada no compêndio; uso como magia e recuperação adaptados |
| `animation.angel.expression_serene` | 2,4 s, loop | Demonstração da expressão de repouso |
| `animation.angel.expression_focus` | 2,4 s, loop | Demonstração da concentração |
| `animation.angel.expression_exertion` | 2,4 s, loop | Demonstração de esforço |
| `animation.angel.expression_hurt` | 2,4 s, loop | Demonstração de reação a dano |

O nome `ground_idle` mantém a convenção de autoria dos estudos anteriores; a Angel está visualmente suspensa. Os clipes de expressão incluem a pose base para poderem ser vistos isoladamente. A futura integração deve separar/aplicar as trilhas faciais como camada, sem substituir toda a pose corporal. Não há sons, VFX ou aplicação de dano nesta auditoria.

## Construção e arquivos

Bedrock/Blockbench, cuboides nativos, dimensões inteiras positivas em grade uniforme 8×, Box UV e atlas 1024×1024. Frente North (`-Z`), raiz `angel` em zero e osso `head`. Nomes em minúsculas. Não há escala animada, malhas suavizadas ou textura fotográfica.

Regras locais preservadas em [references/MODELING.md](references/MODELING.md), baseadas nas convenções do [tutorial Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model). O método de integração do Cobblemon não foi transplantado para o GeckoLib.

- `draft/angel.bbmodel`: modelo editável, atlas incorporado e sete clipes.
- `draft/angel.geo.json`, `angel.png`, `angel.animation.json`: arquivos separados de autoria.
- `draft/angel-preview-03.png`: corpo completo e rosto.
- `draft/angel-expressions-03.png` e `.gif`: expressões em close.
- `draft/angel-face-03.png`: frente, três quartos e volume sem cabelo.
- `draft/angel-views-03.png`: frente, três quartos, perfil e costas.
- GIF/MP4 de idle, físico, magia e inspeção em 360°.
- `draft/validation.json`, `native-validation.json`, `framing-validation.json`: verificações offline.

Esses JSONs usam a escala e os eixos de autoria. **Não copiar diretamente para os recursos do mod.** A futura integração deve aplicar a conversão de eixos e escala uma única vez e conferir enquadramento no menu e em combate.

## Verificação e reprodução

O verificador confere dimensões, atlas sem sobreposição, hierarquia, correspondência com o `.bbmodel`, animações sem escala, continuidade angular e contato entre pescoço/cabeça, braços/mãos, quadris/pernas e joelhos/tornozelos em 343 poses. O teste nativo carrega os recursos pelo GeckoLib 4.8.4, sem executar Minecraft. A renderização confere o enquadramento conservador dos ataques.

Com Python 3, NumPy e Pillow:

```text
python build_model.py
python verify_model.py
python connectivity.py
python present_model.py
python present_joints.py
# Exportar as camadas com AngelMenuPreview.java antes da composição do menu.
python preview_menu.py
python package_model.py
```

`present_model.py` usa Segoe UI do Windows e o FFmpeg local indicado no script. `NativeModelCheck.java` usa Java 17 e as dependências do projeto; deve ser executado novamente após alterações de geometria/animação. O pacote inclui as fontes e prévias prontas, não o vídeo completo pesquisado ou caches.

Nenhum recurso de produção, JAR, instalação, commit ou push faz parte desta entrega. A próxima revisão deve avaliar pernas, mãos, leitura no menu e coreografia física provisória. As versões 01 e 02 permanecem preservadas para comparação.

A prancha `draft/angel-polish-comparison-03.png` compara mãos e pernas com câmera e escala iguais. `compare_revision.py` lê também o estudo 02 preservado no workspace; a imagem pronta está incluída no pacote.
