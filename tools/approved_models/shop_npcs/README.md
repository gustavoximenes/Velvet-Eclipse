# Modelos aprovados dos vendedores

Cópias preservadas de Kurosawa 03, Aohige 02 e Mayoido 02, com editável Blockbench, Bedrock, Box UV, textura e animações. A integração nas fontes foi iniciada após o pedido de usar os NPCs para abrir as lojas com clique direito e substituir os demais atendentes por indicadores. Não integra a pesquisa abandonada da Be Blue V.

Executar `python tools/approved_models/export_shop_npcs.py` a partir do projeto para regenerar recursos e configuração. O exportador lê estas cópias; não altera os estudos originais. `integration.json` registra revisões, escala e hashes das fontes. A conversão de eixos é feita somente na exportação; os clipes continuam com os tempos aprovados.

Validar com `gradlew.bat --offline cityShopAccessTest`. Prévia: `python tools/approved_models/preview_shop_access.py`. Nenhum desses comandos empacota ou instala o mod.

Após revisar a integração nas fontes, o usuário autorizou “implemente tudo isso e compile um jar junto de todas as mudanças combinadas”. Os recursos entram na versão 0.6.54-paulownia-shops; a autorização de empacotamento é posterior à aprovação individual dos modelos.
