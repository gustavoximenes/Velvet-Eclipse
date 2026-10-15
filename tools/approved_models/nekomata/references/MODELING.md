# Padrão de modelagem voxel do Velvet Eclipse

## Izanagi aprovado — integração 0.6.7

A revisão 05 foi autorizada para integração. O fluxo substitui o gerador de malha arredondada: 123 cuboides/planos, 34 ossos e atlas de 512×512.

1. `build_study.py` define geometria, atlas e idle preliminar.
2. `animate_study.py` ajusta a mão da arma, resolve o braço de apoio e gera as três animações. Rodar somente a etapa anterior perde esses ajustes.
3. `export_model.py` executa a autoria e copia os recursos para o mod, renomeando os ataques para `action.magic`/`action.physical` e ajustando os tempos ao impacto no tick 86. O idle é `misc.idle` (4,8 s); os ataques exportados têm 2,5 s.
4. `render_study.py --animate` produz prévias do mesmo modelo, com os tempos anteriores à adaptação ao combate.

Python com Pillow e NumPy é necessário apenas para autoria. A geometria usa grade uniforme DENSITY=2; a escala GeckoLib é 0,25 para preservar o Box UV. Não dividir apenas os tamanhos dos cubos: isso alteraria o mapeamento da textura.

Máscara angular, olhos amarelos com pupilas internas, faixa frontal branca, casaco contínuo na cintura e abas articuladas abaixo, forro vermelho, fechos pintados e dedos separados. A arma é filha da mão direita. Os ataques foram propostos a partir de imagens estáticas, sem captura de movimento do jogo original.

## Fontes técnicas

O tutorial do Cobblemon adota modelos Bedrock, Box UV, hierarquia de ossos e pivôs, orientação North e dimensões inteiras dos cubos. Modelo, PNG e animações são recursos separados. Essas convenções servem de base ao protótipo; os mecanismos de integração específicos do Cobblemon não devem ser transplantados para o GeckoLib do Velvet Eclipse.

Fonte: [Creating a Model — Cobblemon Wiki](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model), consultada em 12/09/2026.

O guia de estilo do Blockbench recomenda que a geometria estabeleça a forma e a textura concentre detalhes. Inclinações devem vir de elementos rotacionados; curvas não devem ser simuladas por uma sequência excessiva de elementos. A densidade de texels precisa ser consistente. A pintura deve usar paleta limitada e agrupamentos deliberados de pixels.

Fonte: [Minecraft Style Guide — Blockbench](https://blockbench.net/wiki/guides/minecraft-style-guide/), consultada em 12/09/2026.

## Padrão proposto para novas personas

1. Definir uma referência principal e listar cinco características que tornam o personagem reconhecível. Referências secundárias apenas esclarecem o que estiver oculto.
2. Fixar proporções, altura, orientação e escala de pixels. Fazer o corpo com poucos cuboides; conferir silhueta antes de decorar.
3. Resolver cabeça e rosto cedo. Comparar de frente e em três quartos, incluindo em tamanho pequeno. Não substituir a máscara específica por um capacete genérico.
4. Usar cuboides e planos nativos com Box UV. Superfícies arredondadas, cilindros, elipsoides e subdivisões suaves não fazem parte deste padrão. Um cubo rotacionado pode resolver uma diagonal; mais peças só quando alteram o desenho de forma necessária.
5. Pintar costuras, símbolos, olhos e materiais em pixels nítidos. Sem ruído aleatório, textura fotográfica, blur ou brilho que esconda a forma. Manter um pixel por unidade de modelagem, usando escala global posterior se necessário.
6. Organizar cabeça, tronco, membros, abas, fitas e arma em grupos com pivôs articuláveis. A roupa deve acompanhar o corpo; os pés definem a altura zero. Evitar interseções na pose inicial.
7. Apresentar render do modelo real: vista principal, frente e detalhe do rosto, com luz simples e sem efeitos que escondam problemas. Arquivo de geometria e PNG devem permitir reproduzir a imagem.
8. Apresentar o modelo real para avaliação antes de integrar uma nova Persona. Após aprovação, exportar os recursos, validar no carregador GeckoLib e executar as verificações de combate antes da entrega.

Estas escolhas artísticas são o padrão proposto do Velvet Eclipse, inspirado nas fontes acima e no pedido do usuário. Não são apresentadas como regras universais nem como uma reprodução de todos os modelos do Cobblemon. Os recursos deste estudo são próprios; nenhum modelo ou textura do Cobblemon foi copiado.


## Conversão validada na 0.6.8

A prévia usa coordenadas cartesianas; o exportador compensa a inversão de X e das rotações X/Y aplicada pelo GeckoLib. A conversão ocorre uma vez, em geometria e animações. Cubos/planos de autoria permanecem iguais; os planos recebem 0,08 unidade de espessura exportada com UV explícito para impedir sobreposição das faces. Não copiar o JSON do draft diretamente para o jogo.

IzanagiAudit compara pivôs/rotações nativos com a pose aprovada e exporta os vértices de idle e ataques após a leitura real do GeckoLib para `build/reports/izanagi/native-*.json`.
