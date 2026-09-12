# Velvet Eclipse 0.6.7 — Izanagi voxel

Minecraft 1.20.1 / Forge 47.4.10 / Java 17. Dependências: GeckoLib 4.8.4, PlayerAnimator 1.0.2-rc1 e Bendy Lib 4.0.0.

## Izanagi

O modelo aprovado substitui integralmente a malha arredondada anterior: 123 cuboides/planos nativos, 34 ossos e atlas pixelado de 512×512. Máscara segmentada com pupilas internas, casaco longo contínuo com forro vermelho, mãos articuladas, pés estreitos, fitas e lâmina presa à mão. Os olhos têm máscara emissiva.

O menu usa o idle de 4,8 segundos. A animação mágica recolhe e abre os braços; a física prepara um corte horizontal com apoio das duas mãos. As poses aprovadas foram mantidas, com tempos ajustados ao impacto do combate. Arma, casaco e fitas acompanham os ossos. A escala global preserva o atlas e é compartilhada pelo menu e pelo mundo.

Zio mostra raios ramificados brancos com halo lilás descendo sobre o inimigo, seguidos de um clarão branco/rosa e faíscas no impacto. O efeito não cria uma entidade de relâmpago que cause fogo ou dano adicional.

## Comandos e habilidades

- `/velvet persona add izanagi`: adiciona Izanagi à seleção.
- `/velvet persona equip izanagi`: equipa fora de combate; durante a batalha, Q/E troca Persona no seletor.
- `/velvet persona remove izanagi`: remove fora de combate. Outra Persona é equipada se necessário; a última Persona não pode ser removida.
- `give` continua disponível; add/remove oferecem sugestões de IDs.

| Habilidade | Nível | Custo | Efeito |
| --- | --- | --- | --- |
| Zio | 1 | 4 SP | Dano elétrico leve a um inimigo |
| Cleave | 1 | 5% HP | Dano físico leve a um inimigo |
| Rakukaja | 1 | 12 SP | Aumenta a defesa de um aliado por 3 turnos |
| Rakunda | 3 | 12 SP | Reduz a defesa de um inimigo por 3 turnos |
| Tarukaja | 5 | 12 SP | Aumenta o ataque de um aliado por 3 turnos |

Conjunto inicial baseado em [Persona 4](https://persona4.wikidot.com/persona), conferido com a [tabela de Izanagi](https://aqiu384.github.io/megaten-fusion-tool/p4g/personas/Izanagi). O cálculo de dano e EXP continuam adaptados ao mod. Cleave do Izanagi usa ID `izanagi_cleave`, preservando o balanceamento das Personas antigas. A família Mazio/Zionga/Mazionga/Ziodyne/Maziodyne permanece no registro, sem ser concedida automaticamente a este Izanagi inicial.

Rakukaja/Tarukaja afetam o jogador no combate atual de um aliado: selecionar a habilidade confirma esse alvo. Rakunda usa a seleção de inimigo. Buffs não causam dano, não descobrem fraquezas e não ativam cut-in. Repetir renova a duração sem acumular multiplicadores; Rakukaja e Rakunda se cancelam. Ataque recebe ×1,33; defesa recebe ×1,33 ou ×0,66 no cálculo de dano. A duração avança com os turnos do alvo, sem consumir um turno extra em ONE MORE.

## Correção do ATTACK

Terreno bloqueado ou falta de apoio no chão não descarta mais um ataque válido. O personagem avança pelo trecho livre e executa o golpe. Quando o terreno encurta a aproximação, o efeito de corte alcança o alvo. Obstáculos surgidos durante a animação interrompem o deslocamento, preservando o impacto reservado. Mantidos dano cortante, impacto no tick 19, retorno e ausência de cut-in para ataques normais, inclusive quando acertam fraqueza.

## Compilação e autoria

```powershell
./gradlew.bat check --offline
./gradlew.bat assemble --offline
```

JAVA_HOME deve apontar para Java 17. Fontes, recursos, testes, dependências locais e wrapper Gradle 8.8 incluídos. O primeiro uso do wrapper precisa baixar Gradle se não estiver no cache. Projeto com nomes SRG e plugin Java, sem ForgeGradle/runClient. A origem das fontes recuperadas está em `source-provenance.json`.

Com Python, Pillow e NumPy, `python tools/izanagi/export_model.py` gera os recursos finais. `python tools/izanagi/render_study.py --animate` gera prévias de autoria (tempos anteriores à adaptação ao combate). Padrão de modelagem em [tools/izanagi/MODELING.md](tools/izanagi/MODELING.md).

As verificações offline cobrem leitura real da geometria pelo GeckoLib, cuboides, animações, habilidades iniciais, suporte, comandos, aproximação bloqueada, golpes em grupo e regressões anteriores. Não houve teste dentro do Minecraft. As prévias são renders dos recursos, não capturas do jogo.

Fontes e JAR de cada versão são publicados na pasta `compiled Jar` de https://github.com/gustavoximenes/Velvet-Eclipse. A instalação no perfil `yuxio test` arquiva a versão anterior e verifica os hashes. Reinicie o perfil para carregar o novo JAR; cliente e servidor devem usar a mesma versão.
