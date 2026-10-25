# Velvet Eclipse 0.6.56 — Onmoraki, ataques e efeitos

24/10/2026. Integração autorizada dos estudos anteriores e correção da ordem de invocação, animação e resultado.

## Sequência de combate

O servidor lê a duração dos clipes `action.physical` e `action.magic` dos mesmos recursos usados pelo GeckoLib. O dano, a alteração de HP/SP, o resultado de fraqueza/One More, os efeitos no alvo e os sons de impacto são aplicados somente depois do clipe completo. Isso também vale para habilidades em área, buffs e curas de Personas.

A Evoker continua disparando no tick 38. A voz original em inglês chama o nome da Persona nesse disparo, uma vez por invocação; o One More conserva seu próprio efeito sonoro. Aparição no tick 46, aura no 54 e início da ação no 55. O relógio inclui os três ticks da transição GeckoLib e quatro ticks de margem para atualização da entidade, seguidos do resultado. A câmera dos participantes recebe a mesma duração, mantém o enquadramento da Persona durante a ação e abre a cena do alvo no resultado. Há quarenta ticks para exibir o efeito antes de liberar a próxima ação.

Exemplos a 20 ticks/s:

| Clipe | Duração | Fim nominal com transição | Resultado | Fim da cena |
|---|---:|---:|---:|---:|
| Personas com clipes de 2,5 s | 50 ticks | 108 | 113 | 153 |
| Onmoraki físico | 48 ticks | 106 | 111 | 151 |
| Onmoraki mágico | 56 ticks | 114 | 119 | 159 |

Cada resultado pertence à reserva e sessão originais. Cancelamento, desconexão, morte ou troca de dimensão não podem deixar um dano atrasado para outra ação. A reserva só pode ser consumida uma vez. Ataque básico com arma mantém sua coreografia própria.

## Onmoraki 01

Substitui o modelo anterior usando o mesmo ID, item criativo e dados de progressão. São 96 cuboides, 20 ossos, Box UV e atlas de 1024², com uma única conversão de eixos. Escala uniforme de 0,125 no renderer. Inclui idle de 3,6 s, ataque físico de 2,4 s e mágico de 2,8 s, enquadramento recalculado nos 181 perfis, carta de fusão e ícone de colocação atualizados.

Os movimentos desta revisão continuam sendo as propostas autorais apresentadas na auditoria: não foram confirmados os clipes específicos de Onmoraki no jogo original. A integração não muda essa limitação. Fonte editável em `tools/approved_models/onmoraki`; exportador em `tools/approved_models/import_onmoraki.py`.

## Photon elemental 02

Integra 22 receitas e 38 habilidades: Agi, Bufu, Garu, Zio, Kouha, Eiha e seus níveis/variantes mapeados, incluindo Inferno, Diamond Dust, Panta Rhei e Thunder Reign. Formas, crescimento, cores e flipbook são os da auditoria aprovada. Os efeitos em área aparecem nos alvos efetivamente atingidos; erro, bloqueio, absorção e reflexão conservam os resultados correspondentes.

A primeira fase visual é alinhada ao resultado confirmado, mantendo os intervalos internos da receita. Um único sistema dispara cada efeito: sem sobrepor a versão antiga. As instâncias têm duração finita e teto compartilhado de 256 componentes por cliente. Grupos muito numerosos reduzem detalhes secundários.

A integração corrige diferenças da API Photon 1.1.17: os modelos usam tamanho/rotação da partícula, os OBJ compensam a origem de meio bloco, e os billboards compensam seus vértices de -1 a 1. Os modelos são registrados no carregamento de recursos. Dados da auditoria e exportação reproduzível em `tools/vfx/approved02` e `tools/vfx/export_elementals.py`.

O fogo da evocação permanece preservado. Buffs/debuffs, cura, físico, Almighty, Nuclear, PSY, Hama/Mudo e especiais fora da auditoria mantêm seus visuais da 0.6.55. O ID existente `eiga` preserva o nome e os atributos; seu efeito usa a referência de fonte 70 (Eiha). Maralagidyne permanece fora das novas receitas. As variantes em área ainda não têm comparação individual de timing com todos os clipes originais.

## Conteúdo preservado e validação

Inclui os áudios originais, mixer separado por categoria, modelos, NPCs, cidade, dificuldade e correções anteriores. Não há nova regeneração da cidade nesta entrega. Os sons não passaram a depender do volume de música do Minecraft.

Verificações offline aprovadas: 8.407 verificações da sequência/reservas; 27.597 da configuração nativa Photon, origem dos OBJ e recursos; 867.541 da geometria e animações GeckoLib; 1.396 de áudio/feedback; 4.421 de Evoker/fraqueza; 4.099 de multiplayer; 496.361 de coreografia/câmera; 104.256 limites de enquadramento dos 181 perfis. Conferidas também integridade do pacote, inclusão do item existente e preservação dos recursos de evocação/áudio/cidade.

Não houve execução do Minecraft. As prévias são renders offline da geometria integrada; iluminação, transparência e sincronização sob latência real ainda dependem de observação no jogo.

Cliente e servidor precisam da **0.6.56-persona-timing**, protocolo principal **24**; cidade **9**, planta **7**, menu **7**. O pacote completo inclui fontes, wrapper, recursos, ferramentas e dependências. A instalação preserva a 0.6.55 em `velvet-eclipse-versions`.
