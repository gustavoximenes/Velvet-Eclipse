# Inicialização com OptiFine — 0.6.31

## Falha identificada

O relatório fornecido pelo usuário e o crash local de 27/09/2026 apresentam a mesma causa fatal:

```text
Critical injection failure: Redirector injectStateToModelLocation
in ldlib-common.mixins.json:ModelBakeryMixin failed injection check, (0/1) succeeded.
```

Ambiente analisado: Minecraft 1.20.1, Forge 47.4.10, OptiFine `1.20.1_HD_U_I6_pre6`, LDLib `1.0.52.a`, Photon `1.1.17` e Velvet Eclipse 0.6.30. A falha acontece durante a construção da LDLib, antes da entrada no jogo. As mensagens posteriores de eventos recusados decorrem do estado de carregamento já quebrado.

A LDLib redireciona `Logger.warn(String, Object[])` dentro de `ModelBakery.getModel`. O OptiFine troca essa chamada pela sobrecarga `Logger.warn(String, Object, Object)`. Como a injeção da LDLib exige pelo menos uma ocorrência, o carregador interrompe a inicialização. A conferência usou a classe produzida pelo patch do próprio OptiFine, reconstruída a partir do cliente vanilla e validada pelo MD5 incluído nele (`9fbe39ca185282a480b7997d6b0a86bb`).

## Correção

Uma configuração Mixin exclusiva do cliente registra uma ponte com prioridade 2000. No callback `preApply`, antes de preparar as injeções da LDLib, `ModelBakeryLogBridge` empacota os dois argumentos da chamada de aviso num `Object[]`. A mensagem e as duas referências são preservadas na mesma ordem. A LDLib volta a encontrar seu ponto de redirecionamento obrigatório.

O código atua somente no método de consulta de modelos da classe `ModelBakery`. Se a chamada com array já existe, como no Forge sem OptiFine, não modifica nada. A operação é idempotente. Não remove métodos, não desativa injeções da LDLib e não altera o fluxo de modelos ou shaders do OptiFine.

OptiFine, LDLib e Photon permanecem nos mesmos JARs originais. A correção é compilada a partir do código-fonte do Velvet Eclipse; não há edição binária das bibliotecas nem redistribuição de classes do Minecraft ou do OptiFine. ASM e Mixin em `dependencies/` são APIs de compilação e teste já fornecidas pelo Forge em execução: não devem ser copiadas para `mods/`.

Atualize apenas o JAR Velvet Eclipse para `velvet-eclipse-0.6.31-optifine-compat.jar`. As dependências de jogo e o protocolo 17 continuam os mesmos da 0.6.30. O perfil `yuxio test` mantém o OptiFine instalado, conforme escolha expressa do usuário.

## Verificação

- `optifineCompatTest`: 158 verificações offline com a classe real reconstruída do OptiFine. O ponto exigido pela LDLib passa de zero para uma ocorrência.
- Execução de uma classe sintética adaptada na JVM: uma chamada de log, mesma mensagem, mesmos objetos e ordem; nomes SRG e de desenvolvimento cobertos.
- Todos os demais métodos da classe OptiFine preservados. Análise de fluxo de dados ASM antes/depois da serialização; aplicação repetida e classe vanilla sem alterações adicionais.
- Configuração exclusiva do cliente e prioridade presentes no bytecode compilado. O ciclo da implementação Mixin 0.8.5 foi conferido: `preApply` ocorre antes da preparação das injeções.
- Inspeção adicional de 19 alvos de mixins nas classes alteradas pelo OptiFine: 21 métodos/pontos explícitos de LDLib e Photon presentes, nenhum alvo ausente. Essa inspeção não executa os callbacks nem substitui o carregador Forge.

Para reproduzir a regressão básica, use Java 17 e `gradlew.bat optifineCompatTest --offline`. Para incluir a classe real, execute antes `tools/compat/UnpackOptifine.java` com os JARs locais do cliente vanilla e OptiFine; passe o arquivo resultante a `-PoptifineClass=<caminho>/patched/net/minecraft/client/resources/model/ModelBakery.class`. Não publique os arquivos reconstruídos.

Não houve boot do Minecraft, teste de shaders ou validação visual nesta entrega. A auditoria reproduz e corrige o ponto exato do crash reportado, mas não comprova compatibilidade visual completa com todos os shaders, resource packs ou versões de OptiFine.

Referências de código: [LDLib ModelBakeryMixin](https://github.com/Low-Drag-MC/LDLib-MultiLoader/blob/1.20.1/common/src/main/java/com/lowdragmc/lowdraglib/core/mixins/ModelBakeryMixin.java) e bibliotecas locais fornecidas para o projeto.
