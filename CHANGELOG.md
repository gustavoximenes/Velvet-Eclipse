# Velvet Eclipse 0.6.5 — comandos de combate

- Faixa de olhos durante a invocação confirmada pelo servidor, antes do dano. Câmera frontal e fragmentação a partir do tick 38, junto ao gatilho. Mantidos o giro e a pose do Evoker. A faixa termina no tick 51; o dano de Persona ocorre no impacto dos efeitos, tick 86.
- O servidor reserva uma única rolagem válida sem gastar HP/SP nem aplicar dano antecipadamente. Erros e ataques sem fraqueza não mostram a faixa. Ataques comuns de espada nunca a mostram, mesmo causando fraqueza.
- Ataque comum com aproximação, corte no tick 19 e retorno até o tick 44. Verificação de colisão do corpo antes do movimento; um caminho bloqueado exige outra ação. Golpes finais preservam o encerramento da animação. A espada permanece visível na espera de combate e usa o elemento SLASH, com compatibilidade com as afinidades físicas antigas.
- Seletor de habilidades com painel azul diagonal, retrato do jogador em azul/branco, Evoker na têmpora, custos, descrições, destaque de seleção e troca de Personas por Q/E. Usa geometria normal/slim e uma cópia da textura da skin, sem modificar a skin original.
- Selecione a habilidade ou ATTACK, confira o alvo e confirme com ENTER ou clique. SETAS escolhem habilidades; Q/E troca Persona; ESC ou botão direito volta. O cursor fica livre durante a batalha e é restaurado na saída.
- A espada de espera fica inclinada para fora, para a frente e para baixo; a transição do punho e a volta da câmera após o corte são suaves. A seleção respeita a página de habilidades e os atalhos ficam suspensos enquanto o chat ou outra tela estiver aberto.
- Removidos os painéis fixos de HP dos inimigos. O marcador acompanha o ator selecionado na arena e troca o ícone conforme o elemento. “?” indica elemento ainda não observado; “! WEAK” indica fraqueza conhecida; outros resultados conhecidos não têm aviso extra. Descobertas são persistidas por tipo de inimigo e elemento; erros não revelam afinidade.

## Validação e limites

Compilação Java 17/Gradle 8.8 offline aprovada. Passaram as auditorias de lógica, apresentação, postura, Evoker e comandos de combate. Comparações entre resolução imediata e reservada cobriram 1.920 cenários; os 120 combates antigos mantiveram os mesmos resultados. Foram verificados codecs, custos, cancelamento, colisão matemática da trajetória, tempos de animação, transparência da paleta, 20 resoluções/escalas e geometria Bendy normal/slim.

Não foi aberto Minecraft/Forge. A composição final na GPU, o carregamento da skin e o comportamento da câmera em jogo ainda precisam de conferência visual no perfil. As prévias offline usam skin ilustrativa, não são capturas do jogo atualizado. O formato atual continua sendo uma batalha com um inimigo; o marcador seleciona esse ator.

Não foi instalado novo mod de API. O projeto inclui a biblioteca lwjgl-opengl 3.3.1 já distribuída pelo Minecraft para compilar o acesso à textura. Cliente e servidor precisam desta versão, pois o canal principal passou ao protocolo 4.

A regra de fechamento aos 95% de uso foi registrada em AGENTS.md. O JAR final foi gerado após a conclusão das alterações e da revisão offline.

## Arquivos

JAR: velvet-eclipse-0.6.5-battle-command.jar. Reinicie o perfil yuxio test para carregá-lo.
A versão 0.6.4 permanece arquivada em velvet-eclipse-versions/0.6.4.
O ZIP do projeto contém todas as fontes, recursos, wrapper Gradle, testes e dependências locais. Com Java 17: `./gradlew.bat build --offline`. O projeto usa fontes SRG e o plugin Java, sem ForgeGradle/runClient. Não utiliza edição binária de JAR como fonte de entrega. Consulte source-provenance.json.

SHA-256: `724D1BD5666C240D2145907ACD8345D98D54149A19C1EEBA6F585E8F1905493E`
