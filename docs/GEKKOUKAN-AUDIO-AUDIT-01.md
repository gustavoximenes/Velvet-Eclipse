# Gekkoukan, móveis e auditoria sonora 01

20/10/2026. Alterações nas fontes; nenhuma nova instalação, publicação ou geração de JAR nesta etapa. A versão instalada continua 0.6.54-paulownia-shops.

## Correções da cidade

- Pilares do hall prolongados até o piso, com verga sobre a entrada. O suporte central superior termina na verga, sem bloquear o acesso.
- Fechamento das laterais superiores na união entre hall e alas. As circulações dos três pavimentos permanecem abertas.
- Vidraças do hall divididas por vãos e travessas, com molduras nas galerias. A prévia agora desenha o vidro em uma passagem transparente separada; sua iluminação continua aproximada e não representa validação de shaders no Minecraft.
- Cadeiras orientadas a partir do encosto físico. Corrigido também o sinal da rotação em salas giradas e assentos do mall. Altura calculada usando o deslocamento de montaria e o quadril do modelo nativo do jogador.
- Corpo do jogador acompanha a orientação do assento, com giro de cabeça limitado. A skin e o modelo nativo permanecem os mesmos.
- Bancos selecionam o lugar mais próximo do clique e procuram outro lugar disponível quando necessário.
- Portas respondem ao clique na parte móvel; paredes e piso compartilhando o bloco não acionam a porta. A verificação de fechamento considera apenas o volume novo ocupado pela folha, preservando o bloqueio quando há alguém em seu caminho.

## Dados existentes

A cidade usa revisão 7 e protocolo 9. A revisão 6 da planta e do mobiliário foi preservada integralmente, junto de **159.702 IDs** e **536.900.052 bytes de vértices** antigos. Novas geometrias foram acrescentadas. A atualização reconhece portas antigas abertas/fechadas, e o diário da Dark Hour é restaurado antes de reconstruir a escola. Não executar novamente o script pontual `work/gekkoukan-audio-01/prepare.py`: ele representa a transição já aplicada.

Para regenerar: `tools/gekkoukan/approved_study.py`, `tools/gekkoukan/export.py`, `tools/city_polish/furniture.py`, `tools/gekkoukan/furniture.py`, `tools/gekkoukan/materials_maps.py`, nesta ordem. Aguardar cada exportação terminar antes de compilar ou copiar recursos. O teste de migração usa o JAR 0.6.54 preservado. O exportador usa `baseline.json` para manter os IDs dessa versão.

## Verificações offline concluídas

| Verificação | Resultado |
|---|---|
| Envoltória da escola | 10.570 pontos da malha; 248 falhas no JAR anterior, nenhuma após as correções |
| Assentos contra encostos e altura real | 116 verificações; 14 cadeiras voltadas no sentido oposto corretamente reconhecidas |
| Móveis, fixações e apoios das escadas | 424 verificações |
| Circulação | Salas e escadas acessíveis segundo o volume de colisão; nenhuma falha |
| Funções da cidade | 4.709 verificações; 173 assentos e 34 portas |
| Geometria e migração | 3.209.068 verificações; 1.068.615 faces não degeneradas e migração das revisões 3–6 |
| Recursos anteriores | Prefixo de metadados/vértices e planta/mobiliário da revisão 6 preservados |

Relatórios em `build/reports/gekkoukan/`; logs em `../work/gekkoukan-audio-01/`. A primeira execução de teste de geometria leu uma cópia intermediária durante a exportação e falhou; a execução final foi feita após a conclusão dos recursos e passou. Não houve teste dentro do Minecraft. A confirmação visual com o conjunto de shaders do usuário continua pendente.

## Auditoria sonora

Painel em `modeling/audio/audit-01/index.html`, com documentação, manifesto e amostras. São 27 efeitos autorais, uma demonstração de One More e oito chamadas em inglês com **voz sintética de guia**. As chamadas não são a voz original nem a atuação final do protagonista. O download do pacote de referência retornou HTTP 403. Nenhum áudio foi inserido em `sounds.json` ou em eventos de execução do mod.

O usuário pediu uma auditoria semelhante à de modelagem: revisar as amostras antes da integração. A próxima etapa é corrigir os timbres e definir a voz final conforme a revisão. Na implementação, a chamada deve corresponder à Persona que executou a ação e ocorrer uma vez quando o próprio acerto em fraqueza gera One More, com isolamento por sessão no multiplayer. Os casos de crítico, item elemental, ataque inválido e múltiplos alvos estão documentados no README da auditoria.
