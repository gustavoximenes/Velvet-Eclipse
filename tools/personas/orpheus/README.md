# Orpheus — modelagem para aprovação, versão 02

A revisão 02 baixa a lira durante o ataque mágico para deixar o rosto livre e mostrar melhor a parte superior do tronco. A transição de retirada e retorno acompanha a altura revisada; a mão esquerda passa a apoiar a lateral do instrumento, enquanto a direita toca as cordas. O idle, o ataque físico, a geometria e a textura permanecem os mesmos da versão 01. O pacote 02 é a referência atual para futura integração.

Estudo independente do Orpheus com corpo turquesa, placas claras, articulações metálicas expostas, cabelo branco assimétrico, rosto cinza com olho vermelho, cachecol vermelho e uma lira clara nas costas. A lira inclui moldura com volutas, sete cordas, caixa de ressonância e ornamento superior em leque. O tórax tem perfurações e o abdômen, um ressonador com cruz.

O modelo faz parte do grupo de três Personas que o usuário pretende integrar futuramente. Arsène está preservado na revisão 02; a terceira Persona ainda será informada. O registro persistente do workspace está em `modeling/MEMORY.md`. Nesta etapa foram criados apenas arquivos de modelagem, sem modificação ou integração no mod.

## Arquivos e uso

Abra `draft/orpheus.bbmodel` no Blockbench. A textura está incorporada, os ossos estão organizados e os três clipes estão disponíveis na aba Animate.

- `draft/orpheus.bbmodel`: projeto editável com 220 cuboides e 31 ossos.
- `draft/orpheus.geo.json`: geometria Bedrock de autoria.
- `draft/orpheus.png`: atlas Box UV 1024 × 1024.
- `draft/orpheus.animation.json`: animações exportadas.
- `draft/orpheus-approval.png`: prancha com idle e poses de ataque.
- `draft/orpheus-idle.gif`, `orpheus-magic.gif`, `orpheus-physical.gif`: prévias dos três clipes.
- `draft/orpheus-magic-poses.png`, `orpheus-physical-poses.png`: sequências das animações.
- `draft/orpheus-front.png`, `orpheus-three-quarter.png`, `orpheus-back.png`, `orpheus-side.png`, `orpheus-details.png`: vistas adicionais.
- `draft/validation.json`, `model-info.json`, `parts.json`: verificações e inventário dos elementos.

## Referências e animações

A segunda imagem enviada orientou as proporções, materiais, cabelo, cachecol, articulações e a lira nas costas. A terceira orientou o ataque físico usando a lira inclinada para baixo e ao lado do corpo, com impulso e abertura das pernas. A quarta orientou a pose mágica com a lira elevada e inclinada à frente.

**A imagem mágica mostra outra Persona. Foram usados apenas o gesto e a posição do instrumento: Orpheus mantém sua paleta turquesa e clara e o cachecol vermelho durante todos os movimentos.** Há um único atlas e nenhuma troca de cor ou material nas animações.

| Clipe | Duração | Movimento |
| --- | --- | --- |
| `animation.orpheus.ground_idle` | 4,8 s | Lira nas costas, respiração discreta e movimento suave de cabelo e cachecol |
| `animation.orpheus.magic_attack` | 3,2 s | Retirada da lira pela lateral, apoio mais baixo à frente, mão esquerda na lateral e mão direita sobre as cordas, retorno |
| `animation.orpheus.physical_attack` | 2,8 s | Preparação com a lira nas mãos, impulso, golpe descendente com a lira baixa ao lado do corpo e retorno |

A posição da lira acompanha as revisões pedidas pelo usuário: ela fica baixa e lateral no físico, e baixa e inclinada à frente no mágico, deixando o rosto visível. Ombros, cotovelos e punhos acompanham pontos definidos no instrumento; os cotovelos usam um eixo de flexão comum para evitar torções artificiais dos antebraços. Os ataques retornam ao idle com a lira nas costas.

As animações foram reconstruídas a partir de referências estáticas, com transições e ritmo próprios. Os GIFs repetem a demonstração; apenas o idle tem repetição no arquivo de animação. Partículas, sons e efeitos de combate ficam para a futura integração.

## Padrão técnico

Segue o [manual de criação de modelos do Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model) e a estrutura local do Izanagi/Igor: Bedrock Entity, raiz `orpheus` na origem, frente voltada para −Z, osso `head`, cuboides nativos, Box UV, dimensões inteiras e pivôs nas articulações. Não foram usados cilindros ou malhas suavizadas. A textura usa uma paleta limitada e detalhes em pixels.

A grade foi ampliada uniformemente em quatro vezes para permitir pequenos detalhes com dimensões inteiras. Os eixos são os da autoria; a futura exportação ao GeckoLib deverá aplicar a conversão de eixos e escala uma única vez, seguindo o fluxo do projeto. Não instalar diretamente estes arquivos como recursos do mod sem essa etapa.

## Verificação

Foram conferidos hierarquia, nomes, dimensões, UVs sem sobreposição, textura incorporada, referências de animação, paleta única, continuidade das orientações, fechamento dos clipes e acompanhamento das mãos em relação à lira durante os golpes. As prévias são renderizações dos arquivos entregues, com câmera fixa por clipe, sem síntese de imagem. A validação é de arquivos e renderização offline; não houve teste no Minecraft.

Para reproduzir, use Python 3 com Pillow e NumPy. A apresentação usa Segoe UI do Windows.

```text
python build_model.py
python verify_model.py
python present_model.py
python package_model.py
```

Os geradores escrevem nesta pasta; o empacotador grava o ZIP em `outputs` no workspace. Nenhum deles altera o mod ou o repositório de publicação.
