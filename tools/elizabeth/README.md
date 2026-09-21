# Elizabeth — estudo de modelagem 02

Modelo independente baseado nas duas imagens fornecidas pelo usuário. Vestido azul sem mangas com gola alta, cinco fechos circulares, luvas longas, sapatos azuis, chapéu inclinado, cabelo branco curto e livro com páginas e capa modeladas.

A revisão 02 atende à correção da roupa: a parte inferior começa alinhada à cintura, sem painéis saltando para a frente ou para os lados. A barra mantém apenas uma abertura suave. Rosto, cabelo e roupas foram construídos especificamente para Elizabeth; não reutilizam a cabeça de Igor ou Izanagi.

## Arquivos

- `draft/elizabeth.bbmodel`: projeto editável do Blockbench em formato Bedrock, com textura incorporada, grupos e idle.
- `draft/elizabeth.geo.json`: geometria de autoria com 92 cuboides e 29 ossos.
- `draft/elizabeth.png`: atlas Box UV 512 × 512.
- `draft/elizabeth.animation.json`: idle original discreto de 4,8 segundos, com respiração e pequenos movimentos de cabeça, cabelo, saia e livro. Pés permanecem apoiados.
- `draft/elizabeth-preview-02.png`: prancha com pose, rosto e costas.
- `draft/front.png`, `three-quarter.png`, `back.png`, `side.png`, `face.png`: vistas do modelo real.
- `draft/elizabeth-idle.gif` e `elizabeth-turntable.gif`: prévias de movimento e giro.
- `draft/validation.txt`: conferência estrutural do estudo.

Abra o `.bbmodel` no Blockbench. A textura já está incorporada; também está disponível separadamente. Nenhum arquivo do mod foi alterado, nenhum JAR foi compilado e não foi feita integração ou publicação desta modelagem.

## Padrão aplicado

Entidade Bedrock voltada para North, cuboides nativos de dimensões inteiras, Box UV sem sobreposição, paleta limitada e ossos com pivôs articuláveis. Grade uniforme DENSITY=4, a mesma usada no estudo de Igor. O corpo estabelece a silhueta; fechos, olhos e detalhes do livro são pintados no atlas em pixels nítidos. A autoria mantém os eixos dos estudos existentes; uma futura integração GeckoLib precisará da mesma conversão de eixos/escala adotada em Igor e Izanagi.

Base técnica local: `velvet-eclipse/tools/izanagi/MODELING.md`, `velvet-eclipse/tools/igor/build_model.py` e o renderizador offline de Igor. O padrão local registra o tutorial [Creating a Model — Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model) e o [Minecraft Style Guide — Blockbench](https://blockbench.net/wiki/guides/minecraft-style-guide/). Nenhum modelo ou textura de Cobblemon foi copiado. Referência visual: somente as imagens de Elizabeth fornecidas nesta etapa.

## Reproduzir

Python 3 com Pillow e NumPy:

```text
python build_model.py
python present_model.py
```

As prévias são renderizadas diretamente da geometria, do atlas e das animações do arquivo, sem síntese de imagem. A tipografia da prancha usa Segoe UI do Windows. Os scripts escrevem apenas nesta pasta de modelagem. Não executam exportação para o mod.


## Integração 0.6.24

O modelo aprovado foi integrado em dois pontos: Velvet Room e pavilhão do Tártaro. `export_model.py` converte os eixos para GeckoLib preservando os arquivos editáveis. `build_weapons.py` gera os nove modelos de armas e os recursos criativos; `preview_weapons.py` renderiza a prancha offline. Consulte `../../docs/ELIZABETH-EQUIPMENT.md` para controles, catálogo e adaptações.
