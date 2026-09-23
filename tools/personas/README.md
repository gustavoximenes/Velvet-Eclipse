# Arsène e Orpheus — integração dos modelos aprovados v02

O usuário autorizou a integração dos dois modelos em setembro de 2026. Os README
nas subpastas registram a etapa histórica de aprovação. A integração atual usa os
arquivos `draft/*.bbmodel`, `.geo.json`, `.animation.json` e `.png` dessas versões,
sem reconstruir nem alterar as poses aprovadas.

`python tools/personas/export_models.py` exporta os dois modelos para os recursos
do mod. A conversão de eixos acontece uma única vez, nesta fronteira. Dimensões
inteiras e UVs são preservados; a escala fica no renderer. Os ataques são
sincronizados em 2,5 segundos, com a pose de impacto aos 1,4 segundos do clipe
(tick 86 do combate, considerando os três ticks de transição do controlador).

Arsène usa a revisão com a mão ao lado do rosto. Orpheus usa a lira mais baixa
no ataque mágico e lateral no físico, sempre com a mesma textura turquesa.

`python tools/personas/item_assets.py` gera as Skill Cards e os ícones dos itens
de colocação seguindo o sistema de pixel art do projeto. Pillow é necessário.

`starterPersonasTest` valida o carregamento real pelo GeckoLib e exporta a
geometria das poses e dos efeitos em `build/reports/starter-personas`.
`python tools/personas/preview_runtime.py` renderiza essas geometrias para uma
inspeção offline, com Pillow e NumPy. Essas prévias não são capturas do Minecraft.
