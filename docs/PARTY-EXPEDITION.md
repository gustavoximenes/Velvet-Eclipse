# Velvet Eclipse 0.6.25 — Party e expedição

- **Arco com H:** a flecha só é encaixada durante a preparação. O disparo tem trajeto 3D visível para jogadores próximos, respeita paredes e inicia o encontro no impacto. Não cria flechas coletáveis.
- **Yen:** `/velvet yen add <quantidade> [jogador]`, com permissão de operador. Aceita valores positivos até o limite da carteira.
- **Elizabeth do hall:** nova opção gratuita para restaurar HP, SP, vida do Minecraft e efeitos negativos. Exige proximidade e estar fora de combate.
- **Tab > PARTY:** até oito membros, convites com aceite e validade de dois minutos, saída e remoção pelo líder. A liderança passa ao próximo integrante se o líder sair. Membros e mapa compartilhado sobrevivem à reinicialização do servidor.
- **Compartilhar:** selecione um integrante online, yen ou um item, digite a quantidade e confirme. Inclui consumíveis da coleção, equipamentos desequipados e pilhas do inventário Minecraft. A transferência de inventário exige um slot livre no destinatário. Ambos devem estar fora de combate. As carteiras continuam individuais.
- **Mapa:** setores e baús vistos são unidos entre os membros. A exploração compartilhada persiste até o reset da Dark Hour. Baús coletados deixam de aparecer para todos. Corrigida a sobreposição de TÁRTARO e HALL.
- **Loja:** antes da Sacerdotisa do trem, acessível pela porta do andar 25, apenas os 17 equipamentos iniciais da delegacia de Reload são vendidos. Os consumíveis continuam disponíveis. A primeira vitória libera o catálogo completo para o servidor e avisa os jogadores, inclusive no próximo login. Vitórias anteriores registradas no bestiário também liberam o estoque.
- **Consumíveis ofensivos:** 14 gemas e seis magatamas, disponíveis na loja, no menu de itens de combate e no criativo. Dano base fixo, afinidades, descoberta, reflexão, absorção e 1 More; não exigem uma habilidade na Persona nem gastam SP. Gemas de gelo e eletricidade podem causar congelamento/choque. Preços adaptados ao mod.
- **F1:** explica o efeito do equipamento ou consumível selecionado. No menu de itens de combate, F1 abre os detalhes do consumível.
- **Ataque básico:** o indicador usa o elemento real do equipamento. Perfurante e concussão possuem ícones e nomes explícitos no bestiário; os três danos físicos são tratados separadamente.
- **Fuga:** chance entre 20% e 60%, calculada pela agilidade. Falhar consome o turno; tentativas repetidas têm intervalo no servidor. Não remove a Shadow do andar. A raid continua sem fuga.
- **Dark Hour+:** oferta em cerca de 5% dos Shuffle Times normais e garantida nos de Shadows vermelhas. Vitória contra uma Shadow vermelha concede Shuffle Time mesmo sem All-Out Attack. Escolher a carta acrescenta cinco minutos à Dark Hour de todos. Fora da Dark Hour, a carta fica guardada para o próximo ciclo.
- **População:** padrão de 5–10 Shadows por andar comum, mantendo uma por sala, sem respawn. Mundos com o antigo padrão 10–20 são migrados; valores personalizados são preservados. Guardiões e transições mantêm suas regras.

## Referências de dados

Estoque anterior a 9 de maio e novas entregas em 10 de maio: [Police Station, guia Reload](https://gamefaqs.gamespot.com/pc/409941-persona-3-reload/faqs/81170/police-station), conferido com [Overworld, megaten-database](https://aqiu384.github.io/megaten-database/p3r/overworld.html). Os equipamentos de personagens recrutados posteriormente não integram o estoque inicial.

Nomes, elementos, alvos e dano base dos consumíveis: [Consumables, guia Reload](https://gamefaqs.gamespot.com/pc/409941-persona-3-reload/faqs/81170/consumables). Modelos e ícones foram construídos no padrão visual do mod. A regra de liberar todo o catálogo após o trem é a adaptação pedida para este servidor.

Validação offline focada; sem iniciar Minecraft, conforme preferência do projeto. A aparência e a sincronização em uma sessão real ainda dependem de validação dentro do jogo.
