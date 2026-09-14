# Velvet Eclipse 0.6.11 — Velvet Room

- O convite All-Out Attack oculta a HUD de combate e as sobreposições comuns. A interface permanece oculta durante a confirmação e a sequência; retorna ao cancelar ou ao terminar o ataque. Cliques durante a confirmação não acionam opções encobertas.
- Formação inicial mais preparada, braço de vitória estendido com dobra menor e novo enquadramento: a cabeça inteira permanece visível nas proporções 4:3, 16:9 e ultrawide. A sequência continua usando o modelo standard/slim e a skin real de cada jogador.
- Aba criativa **Velvet Eclipse** com a **Velvet Key** e os blocos da sala. Chave própria em cuboides, emblema pixelado, haste e dentes geométricos conectados.
- Com a chave na mão, use o botão direito diante de chão firme e dois blocos de altura livres. Uma porta azul aparece à frente. Clique com o botão direito na porta para abri-la e atravesse.
- A porta leva à dimensão própria `velvet_eclipse:velvet_room`: salão azul, piso e molduras, grades ornamentais, mesa e cadeiras. A construção é salva no mundo e não sobrescreve a sala a cada entrada.
- O jogador chega sentado em uma cadeira livre, com câmera por trás e ao lado. Há cinco lugares para visitantes; se todos estiverem ocupados, a entrada permanece possível em pé. **SHIFT** levanta; o botão direito numa cadeira permite sentar novamente.
- A porta atrás das cadeiras retorna ao mundo e às proximidades da entrada de origem de cada jogador. A saída procura espaço livre com chão e evita água e o próprio portal. Assentos vazios são removidos; destruir uma cadeira libera seu ocupante.
- Encontros e geração automática de Shadows ficam suspensos dentro da Velvet Room. Esta versão prepara o espaço para fusões futuras, sem adicionar NPCs ou um novo sistema de fusão.
- Izanagi, suas animações aprovadas, VFX de Zio, fraqueza/cut-in e regras de batalha anteriores preservados. Cliente e servidor devem usar a 0.6.11 (protocolo 7, por causa dos novos registros e da dimensão).

## Verificação

As nove auditorias offline passaram. A auditoria da Velvet Room inclui 17.632 verificações: dados da dimensão, 22 modelos, piso/teto completos, assentos, 32 variantes da porta, passagem de um jogador de 0,6 × 1,8 bloco pelas duas dobradiças e geometria real standard/slim das poses. As oito auditorias anteriores também passaram. Prévias da sala, da chave e da porta foram renderizadas a partir da planta e dos JSONs de produção.

A execução dentro do Minecraft foi dispensada pelo usuário. Não houve teste de teleporte numa partida nem sessão real com vários clientes nesta revisão; as verificações offline não substituem esses testes. O carregador completo Forge depende do launcher e de suas transformações de acesso e não foi executado pelo teste isolado de recursos.

A regra de fechamento antecipado por uso da sessão foi revogada: todas as correções e verificações foram concluídas antes de gerar o JAR final.

Autoria: `tools/build_velvet_room.py`. Auditoria: `gradlew check --offline`. Prévias: `tools/preview_velvet_room.py` após a auditoria. As ferramentas Python usam Pillow e NumPy apenas na autoria.
