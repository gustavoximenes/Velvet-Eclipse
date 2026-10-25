# Próxima versão — chamada nominal na Evoker

Pedido de 23/10/2026: substituir a regra nominal de One More da 0.6.55.

- `PersonaCinematicServer` passa o ID da Persona da sequência a `CinematicEffects`.
- `InvocationFeedback` emite o som da Evoker e a chamada nominal no tick 38. Os demais marcadores sonoros permanecem nos ticks 46 e 54.
- `FeedbackServer.invocation` envia os eventos somente aos membros da batalha atual, no ponto do invocador.
- `FeedbackServer.result` não emite mais a chamada nominal. Mantém sons de fraqueza, derrubamento e One More confirmados.
- O mixer e os arquivos de voz não mudam. Vozes continuam em VOICE, Evoker em COMBAT; baixar Music não silencia nenhum dos dois.
- Cancelamento antes do disparo não gera chamada; a fala não depende de prever fraqueza ou acerto. Preservar o fogo da evocação.

Verificação offline: compilação, `feedbackTest` (1.396 checks) e `evokerWeaknessTest` (4.421 checks). Os casos cobrem o marcador, o ID da Persona, a ausência de repetição no resultado e as regras existentes de sessão/duplicata. Nekomata e Orpheus foram inspecionadas por detecção de silêncio: não possuem silêncio inicial de 30 ms ou mais a −45 dB que justificasse recortar os arquivos novamente.

Sem novo JAR nesta etapa. A auditoria visual de elementais está em `modeling/vfx/audit-02/`; não integrar seus recursos antes da revisão do usuário. Ela não altera buff, debuff, cura, físicos, música nem evocação.
