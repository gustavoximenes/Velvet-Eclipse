# Velvet Eclipse 0.6.18 — terminais e renovação do Tártaro

- Esc fecha a escolha da HUD de combate sem abrir a pausa do Minecraft. Inventário e outros menus continuam bloqueados, inclusive durante os resultados.
- Colocar ou quebrar blocos no Tártaro é bloqueado, incluindo o modo criativo. A Velvet Key orienta a usar as portas fixas, para não acrescentar portas à dungeon.
- Guardiões ficam nos andares 10, 20, 30… 250. O muro tem barreira invisível até o teto e toda a passagem abre depois da vitória.
- Andares 25, 75, 125… são salas fixas de transição, sem Shadows comuns. Nos múltiplos simultâneos de 10 e 25 há guardião, com tesouros na seção depois da barreira. Os vitrais dos andares comuns mudam de paleta por conjuntos de 25.
- Andares comuns geram de um a três tesouros. Cada tesouro concede `250 + andar × 45` yen e um Pulse Tonic ao primeiro jogador que o recolher; a coleta é compartilhada no mundo e não se repete naquele mapa. Transições têm três tesouros fixos.
- Ao amanhecer, jogadores no Tártaro retornam ao mundo. Quem está em combate ou resultados permanece protegido até concluir. No próximo início da Dark Hour, os mapas comuns são renovados sob demanda, com outra semente e novos tesouros. O hall, as salas de guardiões/transição, vitórias e tesouros fixos permanecem. `/time set` também participa desse ciclo.
- Há terminais no hall, nos andares de guardiões e nas transições, separados das portas da Velvet Room. Aproxime-se e pressione **Enter**. Use setas/rolagem e Enter para escolher, ou Esc para fechar.
- Cada jogador desbloqueia um terminal ao visitar seu andar. Os destinos permanecem salvos após reconexão, morte e troca de Dark Hour. O servidor verifica distância e destinos descobertos antes do transporte.
- A porta de Monad no andar **25** é decorativa e selada, reservada para a próxima implementação.
- `/tp tartarus floor <0–250>` continua disponível para operadores; 0 é o hall.

## Compatibilidade e autoria

Andares antigos são reconstruídos na primeira visita nesta versão, preservando o registro de guardiões derrotados. A limpeza cobre a área antiga e a nova antes de montar a sala. Nenhuma reconstrução começa com jogadores em batalha naquele andar. O retorno da Velvet Room passa pelo serviço de geração para não cair em um mapa antigo.

Terminal, tesouro e porta foram feitos com cuboides nativos, Box UV, textura em pixels e hierarquia de partes, conforme o padrão local de Igor e Izanagi. Fontes editáveis e geradores estão em `tools/tartarus_props`. As prévias são renderizações offline da geometria real.

## Validação

Auditorias direcionadas: nova persistência/ciclo, destinos e rede, limite de tesouros, espaço das peças, altura da barreira, salas de guardiões e travessia dos andares. Os três modelos e os idles passam pelo carregador real do GeckoLib. A HUD e os modelos foram revisados em prévias offline.

Não foram executados testes dentro do Minecraft nem com dois clientes, conforme a preferência de entrega do projeto.
