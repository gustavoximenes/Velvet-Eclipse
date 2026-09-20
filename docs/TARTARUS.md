# Tartarus — 0.6.15

A torre agora nivela uma área de 65 × 65 blocos na altura mediana do solo, remove os obstáculos acima dela e apoia a fundação no terreno. Não usa o topo das árvores como altura da torre. As alterações são registradas no mundo e restauradas ao amanhecer ou quando a torre muda de lugar; locais com baús e outras entidades de bloco são evitados. Os tetos dos segmentos da torre foram fechados, incluindo as junções entre os volumes deslocados.

Use `/time set dark_hour` e `/locate structure velvet_eclipse:tartarus`. A porta externa é um portal preto: atravesse para entrar no hall da dimensão `velvet_eclipse:tartarus`. A primeira construção é feita em lotes; aguarde perto da porta enquanto aparece a mensagem de geração.

O hall tem piso azul e marfim, detalhes dourados, colunas, escadaria azul e um relógio monumental. A passagem preta no relógio leva ao andar 1. A porta azul no canto esquerdo leva à Velvet Room e permanece disponível. A passagem preta atrás do ponto de chegada volta ao Overworld.

As portas criadas com a Velvet Key desaparecem após 600 ticks (30 segundos a 20 TPS). A porta fixa do hall e a saída interna da Velvet Room não possuem esse prazo.

Os andares 1 a 250 são criados sob demanda. Cada um tem uma rede conectada de salas e corredores, piso quadriculado, painéis luminosos, colunas, selos no chão e uma escadaria em uma sala distante. O mapa depende da semente do mundo e do número do andar; mudar de andar muda o mapa, e retornar preserva o mesmo layout. Existem três variações de iluminação: verde (1–83), azul (84–166) e violeta (167–250).

A escadaria leva ao próximo andar. O portal de retorno próximo à chegada volta ao anterior. No andar 250, a passagem final retorna ao hall; não existe andar 251.

Cada andar possui sete vagas de Shadows, compartilhadas por todos os jogadores. Uma vaga continua ocupada durante a batalha e fica disponível quando ela termina, com reposição verificada a cada cinco segundos. Não surgem novas Shadows muito perto de um jogador. Descarregar um chunk não libera sua vaga. HP e nível usados nas batalhas aumentam por andar. As Shadows da dungeon permanecem quando a Dark Hour termina no Overworld.

Esta versão utiliza o modelo e as habilidades da Umbra Slime já disponíveis; a progressão aumenta seus atributos. Não inclui chefes ou novas espécies de Shadows. Os interiores continuam acessíveis para quem já entrou, mesmo após o desaparecimento da torre externa, e a saída para o mundo continua funcionando.

## Verificação desta entrega

Auditoria offline focada (`tartarusTest`): conexão entre salas, caminho até as escadarias, altura livre dos degraus, portais, fechamento dos segmentos, estabilidade da geração, limite de 250 andares, progressão de atributos e persistência. Prévia geométrica offline do hall e de um andar. Compilação Java 17 e conferência do conteúdo do JAR. A execução no Minecraft e o teste com dois clientes ficaram para o usuário, conforme autorizado.

Instalar a mesma versão do mod no servidor e nos clientes. Reiniciar o jogo/mundo para carregar a dimensão nova. As dependências existentes são mantidas, sem bibliotecas novas.
