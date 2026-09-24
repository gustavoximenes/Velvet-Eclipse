# Dados do catálogo

Fatos de gameplay (nomes, atributos, resistências, níveis, learnsets, receitas e tabela de arcanos) transcritos de:

- [Megaten Fusion Tool — dados de Persona 3 Reload](https://github.com/aqiu384/megaten-fusion-tool/tree/e93dd1c87ca453de8fae8165bdbef220732feabc/src/app/p3r/data), revisão `e93dd1c87ca453de8fae8165bdbef220732feabc`.
- [Shuffle Time in Persona 3 Reload](https://megatenwiki.com/wiki/Shuffle_Time_in_Persona_3_Reload), tabelas de Personas e ranks por andar consultadas em 23–24/09/2026. A página atribui as tabelas ao guia oficial *Persona 3 Reload Official Complete Guide*, Dengeki/Kadokawa, 2024, pp. 87–95.

Os JSONs em `upstream` fixam a revisão usada. `facts/shuffle-*.json` contém somente as faixas e valores numéricos transcritos, sem depender de capturas de navegador ou arquivos externos. Nenhum código do site ou do calculador foi incorporado.

Nomes alternativos da tabela foram normalizados: Mithra → Mitra, Queen Mab → Queen Medb, Yaksini → Yakshini. A tabela consultada para 12–22F soma 85 em vez de 100; o jogo usa os pesos relativos publicados, normalizados no sorteio, sem inventar uma entrada ausente. Essas probabilidades não devem ser descritas como extração verificada diretamente do executável original.

Ordem das resistências na fonte: corte, impacto, perfuração, fogo, gelo, eletricidade, vento, luz, trevas, onipotente. A coluna de custo codifica SP com acréscimo de 1000; valores menores representam percentual de HP. Filtros excluem as 21 Personas de DLC e a variante interna Mahama B. Eiha/Eiga são unificados por solicitação do usuário.

Para reconstruir, use Python 3 com Pillow:

```text
python tools/catalogue/generate_catalogue.py
python tools/catalogue/generate_shuffle.py
python tools/catalogue/generate_assets.py
```

Os modelos e ícones provisórios são cuboides e rasterização geométrica originais deste projeto. Não são modelos extraídos do jogo. As animações são compatíveis com o carregamento GeckoLib já utilizado no mod. `preview.py` usa Pillow/NumPy e os dumps de `reloadCatalogueTest` para renderizar prévias offline.
