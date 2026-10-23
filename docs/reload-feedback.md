# 0.6.55 — Efeitos, áudio e correções da cidade

O fogo azul da evocação mantém o código, as texturas e a apresentação existentes. Os efeitos de ataque passam a usar as receitas Photon aprovadas, com impacto confirmado pelo servidor, limite de emissores e limpeza ao cancelar, sair do combate ou trocar de dimensão.

- 171 habilidades ativas ligadas a 45 perfis: elementos, golpes físicos, cura, buffs/debuffs, barreiras e limpeza. Ataques em área recebem um orçamento compartilhado de emissores; os efeitos acompanham o alvo quando apropriado.
- 331 trechos originais em OGG mono, provenientes da auditoria 05. 446 rotas contemplam habilidades, itens, armas, portas, móveis, interface e as chamadas de 181 Personas, inclusive sem modelo. As seis Personas autorais usam a chamada genérica original “Persona!”. Sem voz sintetizada ou alteração de pitch.
- Nome da Persona somente no One More causado por um novo derrubamento por fraqueza de uma habilidade. Crítico isolado, item, erro, reflexão, absorção ou fraqueza sobre alvo já derrubado não disparam a chamada nominal. Eventos são enviados aos participantes e deduplicados por sessão.
- Sons de portas e assentos apenas após a interação aceita, uma vez por objeto. Reparo automático não toca som; computadores continuam sem interação nova. Itens também têm som ao serem usados com sucesso fora do combate.
- Revisão 7 da cidade: fechamento de vãos e encontro superior de paredes/janelas da Gekkoukan, pilares até o piso, molduras subdivididas, orientação/altura dos assentos, banco com vaga livre mais próxima e clique na folha móvel das portas. Migração preserva os identificadores da arquitetura anterior.

## Volumes separados

Abra **TAB → System → Áudio do mod**. Use os seis controles por clique/arraste ou pelas setas; Home/End selecionam 0/100%. O idioma acompanha inglês/português do System.

| Controle | Categoria do Minecraft | Alcance |
|---|---|---|
| Música do Minecraft | Music | Altera a opção de música do próprio Minecraft |
| Vozes das Personas | Voice/Speech | Chamadas originais do protagonista |
| Combate e evocação | Players | Ataques, uso de itens em combate, Evoker, invocação e One More |
| Portas e móveis | Blocks | Interações e itens usados fora do combate |
| Interface | Master | Seleção, alvo, confirmação, voltar e erro do menu de batalha |
| Cinemática do despertar | Master | Faixa já existente, que contém música, voz e efeitos misturados |

O volume geral do Minecraft continua valendo para todos. Cada controle do mod multiplica a categoria correspondente; não altera preferências de outros jogadores. Configuração local em `config/velvet-eclipse-audio.properties`. Os arquivos sonoros do despertar foram preservados; a faixa mista ganhou controle próprio, independente de Music. Seus componentes não foram separados.

## Limites desta implementação

As representações são adaptações Photon das referências, não importação dos Niagara originais. PSY reaproveita Pulinpa e Nuclear usa a família Megido. As 13 habilidades especiais ainda usam composições de família: Ghastly Wail, Foul Breath, Stagnant Air, Life Drain, Spirit Drain, Virus Breath, Cadenza, Jack Brothers, King and I, Best Friends, Scarlet Havoc, Trickster e Armageddon. Elas têm feedback funcional, mas não reproduzem integralmente suas sequências exclusivas. Os modelos das Personas não foram alterados nesta versão.

O áudio de abertura da Velvet Room substitui a abertura comum; o fechamento mantém o som anterior, pois não havia um recorte de fechamento aprovado.

## Compatibilidade e validação

Minecraft Forge 1.20.1, Java 17 e dependências Photon/LDLib existentes. Cliente e servidor precisam da mesma versão: protocolo principal 23, cidade 9, menu 7. Sem alteração de balanceamento nesta entrega.

Verificações offline: compilação, causalidade do One More, deduplicação/sessões, pacote com até 128 alvos (incluindo 64 jogadores e inimigos), volumes e persistência, 331 arquivos decodificados, integridade da evocação, táticas, multiplayer, dificuldade, geometria/funções da cidade e idiomas. Não houve execução nem validação visual/sonora dentro do Minecraft.

Os manifestos distribuídos em `data/velvet_eclipse/presentation/` mantêm a procedência e os hashes de cada áudio e a correspondência habilidade → efeito.
