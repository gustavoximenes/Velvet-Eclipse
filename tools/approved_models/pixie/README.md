# Pixie — revisão da geometria 03

Estudo independente para avaliação. As versões 01 e 02 permanecem preservadas. Nenhum recurso do mod foi alterado; não houve integração, build de JAR, instalação, commit ou push.

## O que foi revisado

- A mandíbula da revisão 02 prolongava-se quase horizontalmente para trás. O novo volume inferior inclina-se do queixo até a região abaixo das orelhas, preservando o contorno oval visto de frente.
- Os volumes internos do crânio e da mandíbula foram ajustados a planos de contorno. Peças nas junções completam as superfícies laterais; nenhuma esquina desses volumes ultrapassa os planos definidos.
- Olhos menos projetados: a íris e a pupila são pixels no sólido do olho, em vez de camadas de cubos empilhados. Pálpebras, cílios, sobrancelhas, nariz, lábios e orelhas continuam modelados.
- Ponte e ponta do nariz menores; frente do rosto com junções coplanares para reduzir ressaltos.
- Cabelo com massa principal mais compacta, franja inclinada, laterais ajustadas e nuca curta. A paleta foi escurecida para um castanho-avermelhado.
- Conector do pescoço adicionado à cabeça para manter contato durante os três clipes.

O resultado mantém superfícies e bordas angulares próprias dos cuboides; não é uma malha arredondada. O giro completo e a vista sem cabelo/textura mostram as junções reais, inclusive as pequenas facetas laterais. Não há retoques nas imagens nem iluminação destinada a esconder a geometria.

## Regras e arquivos de autoria

**210 cuboides, 40 ossos, atlas de 1024 × 1024.**

Aplicados o padrão local de `modeling/MEMORY.md`, `velvet-eclipse/tools/izanagi/MODELING.md` e as convenções do [tutorial Creating a Model — Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model): Bedrock Entity, cuboides com dimensões inteiras positivas, Box UV, orientação North (-Z), nomes em minúsculas, raiz na origem e cabeça articulada. A autoria usa ampliação uniforme de 8× para manter um texel por unidade nativa.

A futura integração ao GeckoLib requer a conversão única de eixos e escala prevista no projeto. Os arquivos de autoria não devem ser copiados diretamente para os recursos em uso.

- `draft/pixie.bbmodel`: projeto Blockbench com textura e animações incorporadas.
- `draft/pixie.geo.json`, `pixie.png`, `pixie.animation.json`: recursos separados.
- `head_geometry.py`: feições, superfícies frontais, orelhas e cabelo.
- `skin_boxes.json`: volumes estruturais editáveis da cabeça.
- `fit_head.py`: ferramenta opcional de autoria que ajusta cuboides aos planos de contorno; não gera malha.
- `references/idle.png` e `magic.png`: imagens fornecidas pelo usuário.

## Prévia

- `draft/pixie-face-comparison-03.png`: revisão 02 e 03 na mesma câmera, pose e escala.
- `draft/pixie-face-03.png`: frente, três quartos, perfil e geometria sem cabelo/textura.
- `draft/pixie-face-03.gif`: inspeção lateral da cabeça e da geometria.
- `draft/pixie-head-turntable-03.gif`: giro completo da cabeça.
- `draft/pixie-preview-03.png`, `pixie-views-03.png`, `pixie-details-03.png`: modelo inteiro, vistas e detalhes.
- `draft/pixie-animations-03.gif`: três clipes lado a lado; também há GIFs individuais e sequências de poses.

São renders offline dos recursos entregues, não capturas do Minecraft. As prévias usam os keyframes reais. A comparação da cabeça gira a câmera, mantendo a pose no tempo zero.

## Preservação e validação

Os **58 cuboides do corpo e das asas**, seus pivôs e pixels foram comparados com a Pixie 01 e preservados. Os três clipes também permanecem idênticos nos arquivos de keyframes:

| Clipe | Duração |
| --- | --- |
| `animation.pixie.air_idle` | 4,0 s |
| `animation.pixie.magic_attack` | 2,8 s |
| `animation.pixie.physical_attack` | 1,8 s |

Passaram **7.047 verificações**, incluindo dimensões, hierarquia, UVs, pixels, opacidade das feições e contorno dos volumes estruturais. As junções dos membros e o contato cabeça/pescoço foram conferidos em **243 poses**. Esses testes não constituem aprovação artística nem inspeção dentro do jogo.

O carregador real GeckoLib 4.8.4 leu os **210 cuboides, 40 ossos e três clipes**. Os hashes dos **4.190 arquivos do mod** monitorados permaneceram iguais. Resultados em `draft/validation.json` e `draft/native-validation.json`.

## Reproduzir

Python com NumPy, Pillow e fontes Segoe UI do Windows:

```text
python build_model.py
python present_head.py
python present_model.py
python verify_model.py
```

`skin_boxes.json` já está pronto. Regenerá-lo com `fit_head.py` requer SciPy; a dependência de autoria local não está no pacote e não faz parte do modelo. A comparação visual usa o diretório irmão `pixie-rework-02/draft`; os testes de preservação usam `pixie/draft`. O baseline local do mod não acompanha o ZIP. Validações externas são puladas quando as respectivas fontes não existem.

`NativeModelCheck.java` carrega os recursos com as dependências GeckoLib/Minecraft disponíveis no workspace, sem compilar o mod. `package_model.py` cria o ZIP e verifica cada arquivo contra o manifesto SHA-256.
