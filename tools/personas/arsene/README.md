# Arsène — modelagem para aprovação, versão 02

A revisão 02 afasta lateralmente a mão elevada no idle para deixar a máscara e os olhos visíveis, conforme a nova referência. As entradas e saídas dos ataques acompanham esse idle corrigido. Geometria, textura e movimentos principais dos ataques foram preservados. O pacote 01 permanece como histórico; o pacote 02 é a referência atual para futura integração.

Modelo independente baseado nas sete imagens enviadas nesta etapa. A cartola alta com aba curva, máscara negra com detalhes vermelhos, cravat branco, casaca vermelha, colete estreito com correntes douradas, garras, botas alongadas e asas de penas foram construídos para o Arsène. A estrutura do Izanagi serviu como referência técnica; nenhuma parte da cabeça dele foi reutilizada.

Esta entrega contém somente modelagem, textura e animações. A implementação no mod depende da aprovação do usuário. Não foram alterados recursos de execução, entidades, registros ou configurações do mod; não foi compilado JAR nem publicado commit desta etapa.

## Abrir e conferir

Abra `draft/arsene.bbmodel` no Blockbench. O projeto Bedrock contém os 264 cuboides, 59 ossos, textura incorporada e os três clipes na aba Animate. Escolha um clipe e inicie a reprodução. O PNG também acompanha o projeto separadamente.

| Arquivo | Conteúdo |
| --- | --- |
| `draft/arsene.bbmodel` | Projeto editável completo |
| `draft/arsene.geo.json` | Geometria Bedrock de autoria |
| `draft/arsene.png` | Textura Box UV, 1024 × 1024 |
| `draft/arsene.animation.json` | Os três clipes exportados |
| `draft/arsene-approval.png` | Prancha de apresentação com vista principal, detalhes e costas |
| `draft/arsene-front.png`, `arsene-three-quarter.png`, `arsene-back.png`, `arsene-side.png`, `arsene-details.png` | Vistas adicionais |
| `draft/arsene-idle.gif` | Prévia do idle |
| `draft/arsene-magic.gif` | Prévia do ataque mágico |
| `draft/arsene-physical.gif` | Prévia do ataque físico |
| `draft/arsene-magic-poses.png`, `arsene-physical-poses.png` | Sequências de poses para examinar a trajetória |
| `draft/validation.json`, `model-info.json`, `parts.json` | Validação e inventário da autoria |

## Leitura das referências e movimentos

- **Imagens 1 e 2 — idle:** silhueta longa, cintura estreita, mão com garras junto ao rosto, outra mão à cintura e asas caídas. O idle de **4,8 segundos** acrescenta respiração discreta, movimento das asas, atraso nas pontas das penas e nas abas da casaca. Os pés permanecem apoiados.
- **Imagens 3 e 4 — magia:** preparação com a mão elevada; em seguida, a mão varre de cima para baixo, dirigida ao chão, acompanhada pela inclinação do tronco e abertura das asas. A cabeça compensa a inclinação do corpo para manter o olhar à frente. Duração: **2,8 segundos**.
- **Imagem 5 — físico:** preparação, elevação e extensão da perna, chute diagonal descendente pela frente do corpo e recolhimento. As asas abrem para acompanhar o gesto; as abas da casaca se movem. Duração: **2,6 segundos**.
- **Imagens 6 e 7 — acabamento:** orientaram a casaca, o cravat, as correntes, o formato da cartola, as garras, os saltos metálicos e as penas em camadas.

Nomes dos clipes: `animation.arsene.ground_idle`, `animation.arsene.magic_attack` e `animation.arsene.physical_attack`. Os ataques retornam à orientação do idle; suas prévias GIF repetem a demonstração, mas os clipes exportados de ataque têm `loop: false`. O movimento foi reconstruído a partir das imagens estáticas: o ritmo é uma interpretação de autoria, não uma extração da animação original. Partículas, efeitos de dano e lógica de combate ficam para a futura implementação.

## Padrão técnico

Foi aplicado o tutorial [Creating a Model — Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model), em conjunto com `velvet-eclipse/tools/izanagi/MODELING.md` e o padrão local de estudos do Igor/Elizabeth:

- Formato Bedrock Entity, voltado para North (−Z), raiz `arsene` na origem e osso `head` independente.
- Cuboides nativos, Box UV, nomes minúsculos, pivôs nas articulações e hierarquia de ossos.
- Dimensões inteiras dos cuboides na grade de autoria; ampliação **uniforme de quatro vezes**, incluindo pivôs e posições, para trabalhar detalhes pequenos. A escala de apresentação no jogo será definida durante a integração, sem deformar os cuboides por eixo.
- Paleta limitada, agrupamentos de pixels e detalhes pintados no atlas; sem fotografia, suavização de malha ou ruído aleatório.
- Ombros, cotovelos, punhos, dedos, quadris, joelhos, tornozelos, asas, pontas das penas e abas da casaca articulados.

Os arquivos mantêm os eixos dos estudos locais. Uma futura exportação para GeckoLib deverá aplicar uma única vez a conversão de eixos e escala já adotada no projeto; estes arquivos não foram instalados como recursos do mod.

## Verificação e reprodução

`verify_model.py` confere a hierarquia, dimensões inteiras, UVs sem sobreposição, igualdade do PNG externo e incorporado, referências de animação, transições, retorno à pose de repouso e trajetórias. Também verifica que o olhar se mantém à frente durante a magia e que o tornozelo cruza a diagonal do ponto alto para o baixo no chute. As prévias são renderizadas diretamente dos arquivos entregues, com câmera fixa durante cada clipe. A validação é de arquivos e renderização offline, sem teste no Minecraft nesta etapa.

Requisitos: Python 3, NumPy, Pillow e as fontes Segoe UI do Windows para as pranchas.

```text
python build_model.py
python verify_model.py
python present_model.py
python package_model.py
```

Os geradores escrevem somente nesta pasta. O empacotador grava o ZIP na pasta `outputs` do workspace, sem acessar o mod ou o repositório de publicação.
