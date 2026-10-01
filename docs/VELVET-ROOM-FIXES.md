# Velvet Eclipse 0.6.40 — correções da Velvet Room

## Ambiente e NPCs

- Salas já salvas nas versões anteriores migram automaticamente para o layout 4 ao entrar ou na manutenção periódica dentro da dimensão. Não é preciso apagar o mundo.
- Remove os gradis, luminárias de teto, mesa e cadeira de Igor da construção antiga nos seus pontos conhecidos. Piso, paredes e teto passam a usar materiais de fundo lisos, mantendo a arquitetura nova como decoração visível.
- A mesa redonda tem uma única aparência e colisão aproximada ao seu contorno, alinhada ao tampo. Os blocos decorativos colocados fora da sala continuam com seus modelos originais; não foram criados itens novos.
- Cadeiras ocupadas, corredor de saída, porta e capacidade do grupo são preservados. A limpeza se limita aos blocos antigos previstos no projeto da sala.
- Elizabeth passa a ter um único responsável pela criação. A manutenção espera as entidades do chunk carregarem, recupera uma residente salva e elimina duplicatas sobrepostas na área de atendimento. O mesmo controle protege Igor. A comerciante do Tártaro permanece independente.

## Fusões

- A frente de cada carta mostra o modelo e nome da Persona correspondente à fonte confirmada pelo servidor, inclusive em fusões cooperativas e especiais. A arte do verso permanece separada.
- Retratos foram gerados dos próprios modelos e texturas do mod. O gerador portátil está em `tools/velvet_room/card_portraits.py`; ele não usa capturas ou imagens externas.
- Frente e verso ocupam planos distintos para evitar que o verso cubra o retrato. Dança, voo horizontal e contato na cabeça mantêm seus tempos.
- A câmera da dança foi trazida para dentro do gradil lateral, eliminando barras entre a câmera e as cartas.

## Cor com shaders

A correção adicional é exclusiva do pós-processamento da Velvet Room com shaders OptiFine ativos. Ela reconduz materiais predominantemente azuis que ficaram rosados ao azul e limita o ganho de luz para não saturar isoladamente o canal azul. Tons neutros e de pele quentes são preservados. A paleta dos menus, cenas e renderização sem shaders não foi recolorida.

## Verificação

- `velvetRoomFixTest`: 1.607 verificações de migração, preservação das cadeiras/saída, carregamento tardio de NPCs, recuperação de residente única, retratos de todas as Personas registradas e estados dos blocos.
- `velvetRoomTest`: 31.468 verificações de sala fechada, modelos, assentos, portas e colisões.
- `tools/velvet_room/audit_assets.py`: estrutura dos arquivos de produção, poses rígidas, seis frentes/versos, voo horizontal e câmera dentro do gradil.
- `LightingProbe.java`: GLSL compilado/executado em contexto GPU oculto; 98.304 canais comparados com a curva esperada, incluindo correção seletiva e preservação de neutros/cores quentes.
- Quadros das cartas foram renderizados offline usando os recursos exportados da versão.

Não houve controle do computador ou teste visual dentro do Minecraft. O teste GPU valida o shader isolado; a aparência final sob o pacote de shaders do jogador ainda depende da conferência no jogo. Atualizar cliente e servidor para esta versão. Protocolos mantidos: principal 22, exploração 4, fusão 3 e party 2.
