# Velvet Eclipse 0.6.9 — combates em grupo

- `/velvet battle demo` mantém um inimigo. `/velvet battle demo 1` até `/velvet battle demo 5` define a quantidade; valores fora desse intervalo são rejeitados.
- Cada inimigo tem identidade, HP, conhecimento de afinidades e alvo próprios. Setas esquerda/direita ou cima/baixo alternam os alvos; ENTER confirma. Também é possível selecionar e confirmar pelo mouse.
- Jogadores próximos veem uma nuvem de fumaça escondendo os inimigos. A até 9 blocos do centro do encontro aparece o convite: **J** entra, **N** dispensa até se afastar e se aproximar novamente.
- Ao entrar, o jogador ocupa um espaço livre ao lado do grupo e entra no fim da ordem existente. Há uma única sequência de turnos e uma única resolução de dano. A entrada é recusada se não houver posição segura próxima. Sair/fugir/desconectar remove somente aquele jogador; o restante da equipe continua.
- HP/SP e recompensas são persistidos para cada participante. Vitória concede 80 EXP por inimigo a cada participante que permaneceu na batalha. Todos precisam usar esta mesma versão (protocolo 5), inclusive o servidor.
- Buffs e curas abrem o seletor de aliados. Dia cura 50 HP do aliado escolhido; Media cura 40 HP de todos os aliados vivos. Sylphid possui Dia no nível 3 e aprende Media no nível 6. O conjunto inicial de habilidades do Izanagi foi preservado.
- Zio e outros ataques elétricos recebem raios brancos com halo lilás, convergindo sobre o alvo, e uma estrela de impacto branca/rosa. O efeito é sincronizado para os participantes, inclusive em ataques de área.
- O modelo, textura e animações aprovados do Izanagi foram preservados. A regra do cut-in continua exclusiva de ataques de Persona que acertam uma fraqueza.

Validação: sete auditorias offline, incluindo 4.099 verificações de grupos, 400 reservas de ataques em área, seleção de aliados, comandos e serialização de rede, além das regressões anteriores. Não houve teste dentro do Minecraft nem sessão real com dois clientes; essa validação prática ainda precisa ser feita no jogo.

# Velvet Eclipse 0.6.8 — correção do Izanagi

Correção da conversão entre as coordenadas da prévia aprovada e o GeckoLib: pivôs, rotações dos ossos/cubos e canais das animações preservam a pose original. Isso corrige a posição das mãos, pernas e arma no idle e nos ataques mágico/físico.

As superfícies planas do rosto ganharam uma separação mínima de profundidade, preservando os retângulos da textura. Elimina a disputa entre faces sobrepostas na máscara e nos olhos. A textura aprovada e o desenho do personagem foram preservados.

Validação offline com a geometria e os keyframes carregados pelo GeckoLib; prévias geradas a partir desses dados. Não houve teste dentro do Minecraft. Cut-in, regras de fraqueza, habilidades e combate continuam com o comportamento da 0.6.7.

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
