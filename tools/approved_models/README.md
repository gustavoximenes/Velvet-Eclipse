# Integração 0.6.56

Onmoraki 01 substitui o recurso anterior. `import_onmoraki.py` aplica uma conversão de eixos e preserva 3,6 / 2,4 / 2,8 segundos. `personaFramingExport onmoraki` recalcula apenas seus limites. `preview_onmoraki.py` renderiza os vértices GeckoLib para prévias, carta e ícone. As referências históricas a impacto em 1,4 s abaixo não comandam mais o dano: `PersonaActionTiming` aguarda a animação completa para todas as Personas.

# Cidade e enquadramento da versão 0.6.46

`build_city.py` chama `build_city_blocks.py`: planta revision 2 com blocos visíveis, interiores no volume de seus edifícios, escadas físicas e minimapas por andar. `audit_city.py` chama `audit_city_blocks.py`, que verifica circulação física sem arestas de teleporte. `build_city_mesh_legacy.py` preserva a autoria antiga para consulta; não deve ser usado para exportar a cidade atual.

`gradlew personaFramingExport` mede o ciclo completo das animações nos rigs GeckoLib; exporta envelopes 3D para cartas e envelopes de projeção a cada 5 graus para a ficha. `personaFramingTest` verifica frame e proporções de referência e exporta as camadas do menu. `preview_menu_framing.py` compõe a geometria nativa sobre essas camadas, após `cityIntegrationTest` exportar amostras. Apsaras/Quimera são altas, Jack Frost é menor e Forneus é largo; não se aplica uma altura única a todos.

`preview_city_blocks.py` resolve os blockstates e modelos nativos, usando o JAR local do Minecraft para inspecionar os recursos. Sua exportação permanece em `build/reports/city/blocks`; `CityBlockPreview.java` faz o render em contexto OpenGL oculto. São prévias de geometria, sem simular fielmente um shader pack ou toda a iluminação de Minecraft. Não redistribuir as texturas do Minecraft extraídas em build.

# Cidade e modelos da versão 0.6.45

`import_city_release.py` exporta Apsaras04, Jack Frost01, Forneus01 e Chimera01. Os drafts locais são suficientes para reconstruir os recursos. O giro aprovado de Jack Frost/Forneus também preenche o canal físico.

Na revisão histórica 0.6.45, `build_city_mesh_legacy.py` usa os triângulos e texturas autorais preservados em `city/`, gera a planta de colisão/conectividade e as seções nativas. `audit_city.py` verifica os destinos e balcões. `city/export_shop_art.cjs` reconstrói a arte de cada loja a partir dos SVGs em `city/shop-art.json`.

Após `gradlew cityIntegrationTest`, `preview_city_models.py` renderiza as amostras do GeckoLib e os retratos de fusão das quatro Personas. Requer NumPy/Pillow. O JAR normal usa recursos já exportados, sem exigir Python/Node.

# Modelos aprovados integrados em 0.6.44

Pixie revisão 03 e Elizabeth revisão 05 substituem os modelos anteriores. Os
`README.md` dentro de cada estudo descrevem a etapa histórica de aprovação;
a integração posterior está documentada neste arquivo e em `integration.json`.

## Reproduzir os recursos do mod

1. `python tools/approved_models/export_models.py`
2. `python tools/velvet_room/export_scene.py`
3. Gerar o retrato Pixie: em `tools/velvet_room`, executar
   `python -c "import card_portraits as c; c.card('pixie','Pixie').save(c.OUT/'pixie.png')"`.
4. `gradlew approvedModelsTest battleCameraVisibilityTest velvetRoomFixTest`

O exportador é autocontido e usa os arquivos `draft/` aprovados. A conversão
de eixos ocorre uma única vez na fronteira GeckoLib. Cuboides, Box UV, atlas
e proporções permanecem iguais. A escala global é aplicada no renderer:
Pixie 0,085 e Elizabeth 0,11. Magia e tapa da Pixie duram 2,5 s e atingem
o quadro de contato em 1,4 s, acompanhando o relógio existente de combate.

As fontes Blockbench e os scripts de autoria foram preservados. Scripts
históricos de comparação entre revisões dependem dos estudos anteriores;
não são necessários para exportar, testar ou compilar o projeto. O ajuste
experimental `pixie/fit_head.py` requer scipy; os sólidos já ajustados estão
salvos em `skin_boxes.json`. Exportação: Python 3; cenas e prévias: numpy/Pillow.

`approvedModelsTest` carrega os recursos finais pelo GeckoLib e exporta
amostras de geometria em `build/reports/model-combat`. `preview_runtime.py`
renderiza essas amostras para revisão offline, sem retoque da geometria.
