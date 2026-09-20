# Peças do Tártaro — 0.6.18

Referências: imagens de terminal, tesouro e porta de Monad fornecidas pelo usuário. Convenções técnicas: [padrão do Izanagi](../izanagi/MODELING.md) e [estudo do Igor](../igor/README.md), baseados no manual de modelagem do Cobblemon já consultado no projeto.

`build_models.py` gera Bedrock 1.12, atlas PNG 512×512, máscara emissiva, idle separado e projeto Blockbench. Tamanhos inteiros, Box UV, frente North, um texel por unidade, 16 unidades por bloco. A exportação compensa o eixo X e as rotações X/Y do GeckoLib, como no Igor. As animações destas peças só deslocam a parte luminosa no eixo Y.

- Terminal: 34 cuboides; plataforma octogonal, arco com nervuras pintadas, postes com bobinas, cabos e núcleo flutuante ciano. Base e arco imóveis.
- Tesouro: 15 cuboides; caixa escura, tampa prateada, cintas douradas, fecho ciano e marcador magenta. Some depois da coleta; o registro do mundo impede nova recompensa.
- Monad: 14 cuboides; moldura pontuda escura, contornos vermelhos, três espirais e asas pintadas. Permanece selada, sem teleporte.

`render_models.py` renderiza as vistas frontal e três quartos a partir da geometria e atlas autorais. Python, Pillow e NumPy são necessários apenas para gerar e visualizar os recursos. Nenhum modelo ou textura do Cobblemon/Persona foi copiado.
