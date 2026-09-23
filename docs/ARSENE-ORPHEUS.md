# Velvet Eclipse 0.6.27 — Arsène, Orpheus, Eiga e Agi

Os dois modelos aprovados v02 agora fazem parte das invocações, da ficha de
Persona no Tab, da progressão, da fusão e das recompensas de Shuffle Time.
Arsène preserva a mão lateral no idle e o chute diagonal. Orpheus preserva a
lira abaixada durante a magia e deslocada para o lado durante o ataque físico.
Não há troca de cor no ataque mágico do Orpheus.

| Persona | Nível | Habilidades aprendidas |
| --- | --- | --- |
| Arsène | 1 | Eiga |
| Arsène | 2 | Cleave (6% HP, corte) |
| Arsène | 4 | Sukunda (8 SP) |
| Arsène | 5 | Dream Needle (8% HP, perfuração) |
| Arsène | 7 | Adverse Resolve (passiva) |
| Orpheus | 1 | Agi (3 SP) |
| Orpheus | 3 | Dia |
| Orpheus | 4 | Bash (7% HP, impacto) |
| Orpheus | 5 | Tarunda (8 SP) |

O conjunto do Arsène segue [Persona 5 Royal](https://megatenwiki.com/wiki/Gameplay:Ars%C3%A8ne#Persona_5_Royal).
Por orientação do usuário, o golpe inicial de trevas foi padronizado com o nome
**Eiga** no mod: há uma única habilidade, de dano leve e custo de 4 SP, sem uma
segunda entrada chamada Eiha. Poderes numéricos usam a escala de combate do mod.
Orpheus segue a progressão de [Persona 3 Reload](https://gamefaqs.gamespot.com/pc/409941-persona-3-reload/faqs/81170/personas).

Sukunda reduz a precisão/evasão por três turnos. Tarunda reduz o ataque por três
turnos e cancela Tarukaja. Dream Needle pode causar sono: chance base de 40%,
até três turnos sem agir, despertar ao receber dano, respeitando imunidades.
Adverse Resolve acrescenta 20 pontos à chance de crítico numa batalha iniciada
por emboscada inimiga, enquanto a Persona com a passiva estiver equipada. A
passiva aparece na ficha, mas não ocupa uma opção de ação no menu de combate.

Eiga usa lâminas curvas vermelhas, pontas e clarões ao redor do alvo. Agi usa
uma explosão de fogo, núcleo incandescente, círculo no chão e faíscas radiais.
Os efeitos usam geometria emissiva própria, na mesma infraestrutura de Zio,
enviada pelo servidor para os participantes da batalha. O impacto acompanha o
quadro de dano, inclusive quando a fraqueza resulta numa derrota instantânea.

Ember foi substituído por Agi nas Personas, inimigos, cartas e interface.
Saves e cartas antigos de Ember continuam válidos: a habilidade é convertida
para Agi sem apagar níveis, EXP ou golpes herdados. A carta antiga fica oculta
na aba criativa para não duplicar a nova.

Novas Skill Cards estão na aba correspondente. `arsene_model` e `orpheus_model`
estão na aba de modelos/NPCs e colocam figuras com o mesmo modelo e idle.
As Personas também podem ser obtidas pelos comandos já existentes:

```
/velvet persona add arsene
/velvet persona add orpheus
/velvet persona equip arsene
/velvet persona level arsene 7
```

A capacidade da coleção continua sendo respeitada. Os dois novos modelos não
foram incluídos como inimigos aleatórios. Cliente e servidor precisam usar esta
mesma versão (protocolo principal 15).

Validação offline: cenários executáveis de combate, migração/persistência,
fusões, Shuffle Time, codecs e geometria carregada pelo GeckoLib. Não houve
teste diretamente no Minecraft, conforme a preferência registrada no projeto.
