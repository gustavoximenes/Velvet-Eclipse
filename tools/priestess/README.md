# Sacerdotisa — estudo de modelagem 02

Modelo da personagem das duas referências fornecidas pelo usuário, separado do mod. A revisão refaz a cabeça: rosto mais estreito, queixo afunilado, máscara menor, nariz e lábios discretos, cabelo repartido e tranças com duas mechas entrelaçadas em volume. As pontas das tranças têm articulações próprias.

Abra `draft/priestess.bbmodel` no Blockbench. A textura e as duas animações já estão incorporadas ao projeto. Os arquivos `priestess.geo.json`, `priestess.png` e `priestess.animation.json` também são fornecidos separadamente.

- `misc.idle`: 4,8 segundos, em loop; respiração discreta e movimento das faixas e tranças, mantendo a pose sentada.
- `action.attack`: 2,8 segundos; antecipação, avanço das mãos em direção à frente do modelo, sustentação do gesto e retorno. A animação foi interpretada a partir das imagens estáticas, sem captura de movimento do jogo.
- `priestess-preview-02.png`: prancha do modelo completo, rosto e gesto de ataque.
- `priestess-idle.gif` e `priestess-attack.gif`: animações renderizadas da mesma geometria e textura dos arquivos editáveis.
- `attack-sequence.png`: quatro momentos do ataque.

## Autoria

Python com Pillow e NumPy. Execute, nesta ordem:

```
python build_model.py
python render_model.py
python present_model.py
```

O padrão local de Igor/Izanagi orienta o uso de elementos Bedrock, Box UV, textura em pixels, frente North, dimensões inteiras e pivôs nomeados. A densidade de autoria é uniforme (4); uma futura integração deve usar escala global e a conversão de eixos adequada, sem reduzir isoladamente as dimensões dos cubos. As superfícies inclinadas do rosto usam planos nativos com recorte de textura. Não há malhas suaves.

Referência de fluxo: [Creating a Model — Cobblemon](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model), conforme o manual local estudado. Modelo e texturas foram criados para este estudo; nenhum modelo do Cobblemon ou do jogo foi extraído.

Os JSONs são arquivos de autoria, não recursos prontos para copiar ao GeckoLib. Não houve alteração de código, recursos, JAR ou instalação do mod. A verificação desta entrega é estrutural e visual no renderizador do estudo; não constitui teste dentro do Minecraft.

## Integração 0.6.20

O estudo aprovado foi integrado à raid pela solicitação posterior do usuário. `../raid/build_assets.py` faz a conversão de eixos, a preservação de UV dos planos finos e a cópia dos recursos para o GeckoLib. Os arquivos em `draft` continuam sendo a fonte editável original; o texto acima descreve a entrega de modelagem anterior.
