# Velvet Eclipse 0.6.22 — Shadows de exploração

O contador da Dark Hour fica no canto superior esquerdo. O minimapa ocupa o canto superior direito; M continua expandindo o mapa. O cronômetro da raid permanece independente.

As Shadows comuns agora usam uma silhueta própria fora do combate: massa escura, olhos luminosos e cinco mãos articuladas, com idle e ataque. O projeto Blockbench editável e os geradores estão em `tools/field_shadow/`; a prévia é renderizada da geometria real, fora do Minecraft.

- Visão frontal de 120 graus, até 18 blocos, na mesma sala e com linha de visão livre. São necessários 40 ticks contínuos (dois segundos a 20 TPS) para reconhecer o jogador: `?` durante o reconhecimento e `!` na perseguição.
- Aproximar-se a menos de três blocos fora do campo de visão provoca uma busca com rotação de até 180 graus. Perder o alvo encerra a perseguição. No Tártaro a Shadow permanece na própria sala.
- Ataque pelas costas: todos os aliados da abertura agem antes dos inimigos. Aliados que entram durante essa fase também recebem a vantagem. Ataque frontal/lateral: ordem inicial sorteada. Golpe da Shadow: inimigos começam. Depois da primeira rodada, volta a ordem por agilidade. As regras normais de One More continuam valendo.
- Um encontro comum revela de um a cinco inimigos sorteados do catálogo comum, com nível ajustado ao andar. Chefes, guardiões e Sacerdotisa não entram nesse sorteio. Os atores em combate usam seus respectivos modelos existentes.
- Inimigos alternam ataques físicos e magia conforme os recursos disponíveis. Acertar uma fraqueza pode lhes conceder One More; eles nunca podem executar All-Out Attack.
- Aproximadamente 5% das Shadows são vermelhas. Seus inimigos recebem oito níveis adicionais e multiplicador de HP de 2,2; as recompensas são três vezes a EXP e quatro vezes os yen do encontro comum equivalente.
- Toda vitória sobre Shadows paga yen, apresentados nos resultados e creditados individualmente aos participantes. O valor base é `(60 + 25 × andar) × inimigos`. Nenhuma esmeralda física é usada.

## Uma Shadow por sala

Há no máximo uma Shadow de exploração por sala, sem reposição das derrotadas. A chegada e a escadaria permanecem seguras. O gerador atual possui 14 salas elegíveis: o sorteio configurável de 10–20 fica limitado a essas 14 vagas. `/velvet tartarus shadows <mínimo> <máximo>` mantém o intervalo aceito de 1–64 para compatibilidade, sempre subordinado ao limite de salas.

As vagas excedentes de andares antigos são desativadas; não há reconstrução do terreno nem perda de baús, exploração ou terminais. Shadows antigas restantes são convertidas para o novo modelo na vaga correspondente. O sorteio do grupo e da raridade persiste depois de fugir e ao recarregar o mundo. Andares fixos e guardiões mantêm seu comportamento próprio.

Na aba criativa Velvet Eclipse estão **Invocar Shadow** e **Invocar Shadow Vermelha**. A colocação manual também recusa salas já ocupadas e andares fixos do Tártaro. `/velvet shadow spawn` usa o novo modelo no mundo; o identificador da entidade é `velvet_eclipse:field_shadow`.

Cliente e servidor precisam da mesma versão, protocolo 13. Foram feitas verificações offline de percepção, turnos, magia, recompensas, população e carregamento nativo do modelo/animações. Não foi iniciado o Minecraft nem feito teste multiplayer com clientes reais.
