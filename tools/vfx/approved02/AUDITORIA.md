# Auditoria 02 — ataques elementais de Persona 3 Reload

23/10/2026 · proposta para revisão · sem substituição dos VFX instalados

Esta revisão trata de dano de fogo, gelo, vento, eletricidade, luz e trevas. O fogo azul da evocação permanece como está. Buffs, debuffs, cura, ataques físicos, PSY, Nuclear, Almighty, Teurgias e mortes instantâneas não fazem parte desta revisão. Divine Judgment e Demonic Decree são sequências especiais de redução de HP, fora das cadeias elementais comuns estudadas aqui.

O painel apresenta **22 estudos individuais**: três níveis de cada uma das seis famílias e os quatro ataques severos Inferno, Diamond Dust, Panta Rhei e Thunder Reign. Inclui também **seis estudos em área**, com três alvos ilustrativos. A distribuição em área é uma proposta de aplicação da nova forma por alvo, não uma reprodução homologada de cada animação Ma-.

## Diagnóstico da versão 0.6.55

O problema principal é de composição, não apenas de quantidade ou brilho. `skill-vfx.json` liga todos os níveis de fogo a `agi`, todos os de gelo a `bufu`, e assim sucessivamente. `ReloadPhoton` aplica um fator de tamanho limitado a esses perfis. Aumentar esse fator não cria a esfera de Ziodyne, a expansão de Garudyne ou a fragmentação de Bufudyne.

O renderizador de receitas anterior oferece sprites, trilhas e feixes. Isso é útil para partículas secundárias, mas os cristais continuam planos e o vento perde suas superfícies largas. O novo estudo combina malhas facetadas, faixas curvas com espessura variável, raios segmentados, partículas secundárias e um novo flipbook de explosão de fogo. A evocação não usa esses recursos novos.

| Família | Diferença identificada | Direção da revisão |
|---|---|---|
| Agi | Coluna de fogo genérica no lugar de uma explosão arredondada | Massa de chamas curta, recortes escuros, frente luminosa e brasas |
| Agilao | Mesmo perfil da magia fraca | Abertura das chamas e aro de fogo mais pronunciado |
| Agidyne | Apenas escala maior | Núcleo escuro, cortes circulares e explosão mais densa |
| Inferno | Reuso de Agi | Eruptura e massas de fogo em sequência, em receita própria |
| Bufu | Fragmentos planos e pouco volume | Pequenos pedaços facetados que aparecem e se quebram |
| Bufula | Bufu ampliado | Fragmentos maiores com choque e dispersão mais definidos |
| Bufudyne | Sem a grande forma de gelo do original | Pontas que crescem em volume e estilhaçam para fora |
| Diamond Dust | Reuso de Bufu | Massa de gelo própria e dissolução mais longa |
| Garu/Garula | Hélice fina e uniforme | Rajadas largas e curvas, com bordas afiladas |
| Garudyne/Panta Rhei | Mesmo tipo de hélice | Casca de vento em torno do alvo e expansão na base |
| Zio/Zionga | Violeta uniforme e feixe genérico | Núcleo branco/amarelo e ramificações rápidas |
| Ziodyne | Raio vertical ampliado | Carga azul/violeta envolvendo o alvo e arcos amarelos |
| Thunder Reign | Reuso de Zio | Carga elevada seguida de descarga e onda de impacto |
| Kouha/Kouga/Kougaon | Feixes diagonais genéricos | Aros dourados, descida de luz e pilar progressivamente mais estruturado |
| Eiha/Eiga/Eigaon | Fumaça com poucas trilhas | Erupção vermelha, correntes ascendentes e lâminas magenta |

## Referências e grau de certeza

A fonte visual é o vídeo local **Persona 3 ALL Skills Showcase & Comparison | Portable Vs Reload**, [trecho de magias](https://www.youtube.com/watch?v=TZap-dV5Oyo&t=325s). Ele alterna Portable e Reload: **somente os trechos Reload foram usados nas comparações**. Cada habilidade tem seu clipe individual e o instante de referência registrados no painel e no manifesto. Os clipes foram feitos a partir da cópia local já existente, sem nova aquisição de mídia.

Os arquivos de Niagara extraídos da instalação fornecida pelo usuário confirmam composições distintas. Exemplos:

- `NS_FX_S0010_00`: `Fire_Anim_00`, `Fire_Dark_00`, `Flash_00`, `Shock_00`.
- `NS_FX_S0012_00`: `Black_Sphere_00`, três `Fire_Anim`, `Slash_Ring_00/01`.
- `NS_FX_S0022_00`: quatro camadas `Ice`, duas `Ice_Dust`, brilho e choque.
- `NS_FX_S0032_00`: `Sphere_Aura_00`, `Wind_Anim_00`, folhas, pedras e anel.
- `NS_FX_S0042_00`: duas `Sphere_Aura`, dois `Thund`, `GlowBlack` e dois anéis.
- `NS_FX_S0058_00`: `Charge_Ring`, `Pillar_Core`, `Down_Beam` e `Aura_geyser`.
- `NS_FX_S0072_00`: `Pillar_core`, `Pillar_aura`, correntes ascendentes e espirais.

`evidence.json` conserva nomes, imports e hashes dos assets inspecionados. Esses nomes **não fornecem as curvas completas, valores de materiais ou tempos exatos**. A aparência geral foi observada no gameplay; escala, curvas, número de fragmentos e duração do protótipo são propostas autorais a revisar. Nenhuma fidelidade de pixel ou extração integral de Niagara é alegada.

## O que as prévias mostram

As prévias usam um boneco neutro de 1,8 m, câmera fixa e fundo comum. Servem para comparar forma, escala em relação ao alvo, direção do movimento, sequência de crescimento e dissipação. Os níveis têm receitas distintas, não apenas nomes diferentes para o mesmo efeito.

As 22 receitas e as malhas da visualização também alimentam `PhotonElementalDraft.java`, compilado contra Photon Forge 1.20.1 / 1.1.17 e LDLib do projeto. O protótipo usa `RendererSetting.Particle.Mode.Model`, emissores locais finitos, crescimento/rotação por idade, flipbook 8×4 para fogo, teste de profundidade e sem escrita de profundidade para transparências. Não há registrador de eventos ou ligação ao combate de produção.

**Os vídeos da proposta foram renderizados offline, não pelo Minecraft.** O renderizador de prévia aproxima ordenação de faces, luz e brilho. A compilação comprova compatibilidade de API, não o carregamento visual dos OBJ, a mistura de materiais, custo de GPU ou compatibilidade com shaders. Antes da integração será necessário conferir orientação dos modelos, transparência, escala e contraste no motor. A renderização offline não deve voltar a ser tratada como prova de aparência final.

O fogo já recebeu uma textura animada nova, mas esta ainda é uma proposta, não uma textura extraída do jogo. Gelo e vento priorizam volume e movimento. O material final pode precisar de mais polimento após a aprovação dessas formas.

## Escopo por habilidade e catálogo

`COBERTURA.md` lista os IDs ativos e seus estudos. Há uma divergência preexistente: o ID do mod `eiga` usa fonte 70, que é **Eiha** na referência. O nível intermediário Eiga, fonte 71, não tem uma entrada própria equivalente no catálogo atual. O mesmo cuidado vale para `maeiga`/fonte 73. O painel mostra os três níveis originais separadamente para evitar ocultar o problema. Nenhum nome, custo, dano ou ID foi alterado por esta auditoria.

As variantes Ma- conservam o perfil do nível correspondente por alvo, com composição sonora por ação. As seis prévias em área validam apenas a leitura de três volumes juntos. O timing específico de cada Ma-, inclusive propagação entre alvos, deve ser comparado com seu trecho original antes da implementação final; não considerar esse ponto aprovado por um estudo individual.

## Requisitos para a integração posterior

1. Usar uma receita específica por nível. Não substituir as diferenças por um multiplicador global de escala.
2. Fixar o ponto de impacto nos pés do alvo, com offsets relativos ao corpo. Manter limites para inimigos pequenos e grandes; GUI e distância da câmera não mudam escala de mundo.
3. Alinhar o ápice visual ao resultado confirmado. As receitas da auditoria incluem preparação e têm picos distintos. Não iniciá-las integralmente depois do dano e som, acrescentando atraso. Preparação pode começar a partir da ação armada; acerto, falha, absorção, reflexão e imunidade devem continuar dependentes do resultado do servidor.
4. Rebasear a parte de impacto ao marcador confirmado e cancelar preparação órfã quando a ação falhar. Não disparar dano por uma animação de partícula.
5. Restringir sons e efeitos aos participantes da sessão. Não multiplicar som de área por número de alvos ou espectadores.
6. Preservar o núcleo visual sob redução de qualidade. Reduzir primeiro faíscas, folhas e detritos; não truncar a lista arbitrariamente até apagar o raio ou a massa de gelo.
7. O protótipo prioriza revisão visual, ainda não otimização final: cada peça é um emissor. Antes da integração, agrupar detritos em bursts, compartilhar modelos e limitar custo por ação. Validar cinco alvos e grupos de 1–4 jogadores; nenhuma medição de desempenho foi feita nesta etapa.
8. Descartar emissores em fim/cancelamento da sessão, troca de mundo e perda do alvo. Não reutilizar IDs numéricos sem UUID. Manter profundidade para não desenhar gelo através de paredes.
9. Manter a evocação, os buffs/debuffs e todas as famílias fora do escopo com seus recursos atuais.

## Correção já feita: fala no disparo da Evoker

O nome da Persona agora é emitido pelo servidor no **tick 38**, no mesmo marcador de `summon_evoker`. Usa a Persona armazenada na sequência de invocação e é enviado aos membros daquela batalha. Foi removido o disparo nominal em `FeedbackServer.result`, evitando repetição no One More. O efeito sonoro de One More continua ligado ao resultado.

A mudança se aplica a cada invocação, independentemente de o ataque depois atingir fraqueza, errar ou encerrar a batalha. Cancelar antes do disparo não gera fala. A voz conserva o canal de volume **Vozes**, independente de música e efeitos de combate. Os arquivos de áudio originais e o fogo da evocação permanecem intactos.

Compilação e testes offline: `feedbackTest` passou com 1.396 verificações; `evokerWeaknessTest` passou com 4.421. Não houve teste no Minecraft. Esta etapa não gerou nem instalou um novo JAR; a correção está nas fontes, enquanto os VFX aguardam revisão.

## Decisão visual

Revisar primeiro Agi, Zio, Bufudyne e Garudyne; em seguida os níveis intermediários, severos e luz/trevas. O painel permite pausar no impacto, reduzir a velocidade, registrar comentários por habilidade e exportá-los. Aprovar a direção da proposta não elimina a necessidade de conferir os materiais no motor.
