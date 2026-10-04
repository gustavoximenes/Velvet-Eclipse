# Elizabeth — contorno oval 05

Revisão independente do estudo de modelagem. Recupera um contorno mais oval tomando a revisão 03 como referência, após o usuário apontar que a revisão 04 havia afunilado demais o rosto e deixado o queixo com uma base reta. Mantém o polimento do nariz e o encaixe corrigido dos braços.

Nenhum arquivo do mod foi substituído. Não houve integração, geração de JAR, instalação, commit ou push. As revisões 03 e 04 permanecem preservadas.

## Alterações

- Bochechas e mandíbula mais cheias, com mudanças graduais de inclinação até o queixo.
- Queixo com laterais chanfradas e base mais curta. A inclinação da região facial recua a parte inferior também de perfil.
- Profundidade da mandíbula reduzida em direção ao queixo, evitando uma projeção retangular sob o rosto.
- Superfícies frontais próximas em profundidade, sem a antiga sobreposição de placas salientes. A pele usa a mesma paleta e recebe sombreamento das superfícies reais.
- Nariz menor e contínuo da revisão 04 preservado em suas proporções locais. Olhos, pálpebras, boca e sobrancelhas acompanham a nova inclinação facial.
- Encaixe de braços e ombros, mãos, pose com livro, cabelo, chapéu, roupa e animação de repouso mantidos. A articulação superior do pescoço foi completada para sustentar a nova mandíbula durante o idle.

O contorno é construído com cuboides rotacionados; conserva facetas compatíveis com o estilo do projeto. Não usa subdivisão arredondada nem fotografia de rosto sobre um plano.

## Arquivos

**289 cuboides nativos, 45 ossos, atlas 1024 × 1024, Box UV e idle de 4,8 segundos.**

- `draft/elizabeth.bbmodel`: editável no Blockbench, com textura incorporada e animação.
- `draft/elizabeth.geo.json`, `draft/elizabeth.png`, `draft/elizabeth.animation.json`: geometria, atlas e animação separados.
- `draft/elizabeth-comparison-05.png`: comparação com o primeiro modelo, revisão 03, usando a mesma câmera e escala.
- `draft/elizabeth-preview-05.png`: prancha com modelo inteiro e detalhes do rosto.
- `draft/elizabeth-arms-05.png`: inspeção dos braços e ombros.
- `draft/elizabeth-views-05.png`: frente, três quartos, perfil e costas.
- `draft/elizabeth-face-05.gif`: rosto em movimento, com materiais e em geometria cinza sem cabelo.
- `draft/elizabeth-turntable-05.gif`: giro completo.
- `draft/elizabeth-idle-05.gif`: repouso animado.
- `draft/validation.json` e `draft/native-validation.json`: resultados das verificações.

Todas as prévias são renders offline dos mesmos arquivos entregues. Não são capturas dentro do Minecraft. Nas vistas de inspeção facial, cabelo/chapéu e materiais são removidos somente para revelar a construção.

## Regras e referência

Mantidas as regras locais de `modeling/MEMORY.md` e `velvet-eclipse/tools/izanagi/MODELING.md`, junto ao tutorial [Creating a Model — Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model): formato Bedrock, dimensões inteiras positivas, Box UV, orientação North (`-Z`), nomes em minúsculas, raiz `elizabeth` na origem e osso `head`. Densidade uniforme de autoria em 8×, um texel por unidade nativa.

A referência de Elizabeth continua sendo o vídeo `videoplayback (4).mp4`, estudado para a revisão 03, e o comparativo enviado pelo usuário. Partes ocultas são interpretação autoral dentro do estilo aprovado; não se trata de extração do modelo original.

## Verificação

- Dimensões, UVs sem sobreposição, textura incorporada, hierarquia e curvas de animação conferidos.
- Interseção real de caixas orientadas verifica o encaixe dos braços e a ligação cabeça/pescoço em **49 amostras do idle**.
- Ponte do nariz ligada ao rosto e à ponta, sem escala animada.
- Pés fixos e livro vinculado à mão.
- **223 peças** fora das regiões alteradas mantêm geometria, osso e material da revisão 03. O atlas foi reorganizado.
- Carregador real GeckoLib 4.8.4 leu o modelo e a animação. Esse teste não aplica integração ao jogo.
- Hashes de **4.190 arquivos do mod** preservados.

Não houve controle do computador nem teste dentro do jogo.

## Reprodução

Python 3 com NumPy e Pillow, fonte Segoe UI do Windows:

```text
python build_model.py
python present_model.py
python review_stills.py
python inspect_contour.py
python verify_model.py
```

O comparativo é regenerado quando a pasta irmã `elizabeth-rework-03/draft` está disponível. A imagem já renderizada acompanha o pacote. A verificação do mod exige o workspace original e o arquivo local `source-before.json`, que não acompanha o ZIP; os testes estruturais funcionam sem esse baseline. `NativeModelCheck.java` usa as dependências locais GeckoLib/Minecraft para carregar os arquivos, sem compilar o mod.

Uma integração futura deverá aplicar a conversão única de eixos e escala global conforme o renderer do projeto. O idle é `animation.elizabeth.ground_idle`.
