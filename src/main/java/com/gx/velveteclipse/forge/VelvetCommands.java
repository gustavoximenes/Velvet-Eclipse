package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.core.battle.BattleAction;
import com.gx.velveteclipse.core.fusion.FusionResult;
import com.gx.velveteclipse.core.fusion.FusionService;
import com.gx.velveteclipse.core.persona.PersonaDefinition;
import com.gx.velveteclipse.core.persona.PersonaInstance;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.network.ModNetwork;
import com.gx.velveteclipse.util.RuntimeBridge;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.ArrayList;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;

public final class VelvetCommands {
   private VelvetCommands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> var0) {
      var0.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(
                                          "velvet"
                                       )
                                       .executes(var0x -> help(var0x.getSource())))
                                    .then(LiteralArgumentBuilder.literal("help").executes(var0x -> help(var0x.getSource()))))
                                 .then(LiteralArgumentBuilder.literal("status").executes(var0x -> status(var0x.getSource()))))
                              .then(LiteralArgumentBuilder.literal("restore").executes(var0x -> restore(var0x.getSource()))))
                           .then(LiteralArgumentBuilder.literal("diagnostics").executes(var0x -> diagnostics(var0x.getSource()))))
                        .then(LiteralArgumentBuilder.literal("compendium").executes(var0x -> compendium(var0x.getSource()))))
                     .then(
                        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("persona")
                                    .then(LiteralArgumentBuilder.literal("list").executes(var0x -> listPersonas(var0x.getSource()))))
                                 .then(LiteralArgumentBuilder.literal("owned").executes(var0x -> owned(var0x.getSource()))))
                              .then(
                                 LiteralArgumentBuilder.literal("give")
                                    .then(
                                       RequiredArgumentBuilder.argument("id", StringArgumentType.word())
                                          .executes(var0x -> givePersona(var0x.getSource(), StringArgumentType.getString(var0x, "id")))
                                    )
                              ))
                           .then(
                              LiteralArgumentBuilder.literal("equip")
                                 .then(
                                    RequiredArgumentBuilder.argument("id", StringArgumentType.word())
                                       .executes(var0x -> equipPersona(var0x.getSource(), StringArgumentType.getString(var0x, "id")))
                                 )
                           )
                     ))
                  .then(
                     ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("fusion")
                           .then(
                              LiteralArgumentBuilder.literal("preview")
                                 .then(
                                    RequiredArgumentBuilder.argument("first", StringArgumentType.word())
                                       .then(
                                          RequiredArgumentBuilder.argument("second", StringArgumentType.word())
                                             .executes(
                                                var0x -> previewFusion(
                                                      var0x.getSource(),
                                                      StringArgumentType.getString(var0x, "first"),
                                                      StringArgumentType.getString(var0x, "second")
                                                   )
                                             )
                                       )
                                 )
                           ))
                        .then(
                           LiteralArgumentBuilder.literal("perform")
                              .then(
                                 RequiredArgumentBuilder.argument("first", StringArgumentType.word())
                                    .then(
                                       ((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("second", StringArgumentType.word())
                                             .executes(
                                                var0x -> performFusion(
                                                      var0x.getSource(),
                                                      StringArgumentType.getString(var0x, "first"),
                                                      StringArgumentType.getString(var0x, "second"),
                                                      null
                                                   )
                                             ))
                                          .then(
                                             RequiredArgumentBuilder.argument("skill", StringArgumentType.word())
                                                .executes(
                                                   var0x -> performFusion(
                                                         var0x.getSource(),
                                                         StringArgumentType.getString(var0x, "first"),
                                                         StringArgumentType.getString(var0x, "second"),
                                                         StringArgumentType.getString(var0x, "skill")
                                                      )
                                                )
                                          )
                                    )
                              )
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("shadow")
                           .then(LiteralArgumentBuilder.literal("spawn").executes(var0x -> spawnShadow(var0x.getSource()))))
                        .then(LiteralArgumentBuilder.literal("count").executes(var0x -> countShadows(var0x.getSource()))))
                     .then(LiteralArgumentBuilder.literal("nearest").executes(var0x -> nearestShadow(var0x.getSource())))
               ))
            .then(
               ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(
                                                   "battle"
                                                )
                                                .then(LiteralArgumentBuilder.literal("start").executes(var0x -> startBattle(var0x.getSource()))))
                                             .then(LiteralArgumentBuilder.literal("nearest").executes(var0x -> startNearestBattle(var0x.getSource()))))
                                          .then(LiteralArgumentBuilder.literal("demo").executes(var0x -> demoBattle(var0x.getSource()))))
                                       .then(LiteralArgumentBuilder.literal("attack").executes(var0x -> attack(var0x.getSource()))))
                                    .then(
                                       LiteralArgumentBuilder.literal("skill")
                                          .then(
                                             RequiredArgumentBuilder.argument("id", StringArgumentType.word())
                                                .executes(var0x -> skill(var0x.getSource(), StringArgumentType.getString(var0x, "id")))
                                          )
                                    ))
                                 .then(
                                    LiteralArgumentBuilder.literal("persona")
                                       .then(
                                          RequiredArgumentBuilder.argument("id", StringArgumentType.word())
                                             .executes(var0x -> battlePersona(var0x.getSource(), StringArgumentType.getString(var0x, "id")))
                                       )
                                 ))
                              .then(LiteralArgumentBuilder.literal("guard").executes(var0x -> guard(var0x.getSource()))))
                           .then(LiteralArgumentBuilder.literal("item").executes(var0x -> item(var0x.getSource()))))
                        .then(LiteralArgumentBuilder.literal("escape").executes(var0x -> escape(var0x.getSource()))))
                     .then(LiteralArgumentBuilder.literal("stop").executes(var0x -> stop(var0x.getSource()))))
                  .then(LiteralArgumentBuilder.literal("sync").executes(var0x -> sync(var0x.getSource())))
            )
      );
      var0.register(LiteralArgumentBuilder.<CommandSourceStack>literal("velvet").then(
         LiteralArgumentBuilder.<CommandSourceStack>literal("persona")
          .then(LiteralArgumentBuilder.<CommandSourceStack>literal("add").then(
            RequiredArgumentBuilder.<CommandSourceStack,String>argument("id",StringArgumentType.word())
              .suggests((ctx,b)->{PersonaRegistry.personas().keySet().stream().sorted().filter(id->id.startsWith(b.getRemaining())).forEach(b::suggest);return b.buildFuture();})
              .executes(ctx->givePersona(ctx.getSource(),StringArgumentType.getString(ctx,"id")))))
          .then(LiteralArgumentBuilder.<CommandSourceStack>literal("remove").then(
            RequiredArgumentBuilder.<CommandSourceStack,String>argument("id",StringArgumentType.word())
              .suggests((ctx,b)->{Object p=RuntimeBridge.playerFromSource(ctx.getSource());if(p!=null)PlayerPersonaData.load(p).rosterIds().stream().sorted().filter(id->id.startsWith(b.getRemaining())).forEach(b::suggest);return b.buildFuture();})
              .executes(ctx->removePersona(ctx.getSource(),StringArgumentType.getString(ctx,"id")))))
      ));
   }

   private static Object player(Object var0) {
      Object var1 = RuntimeBridge.playerFromSource(var0);
      if (var1 == null) {
         RuntimeBridge.sendMessage(var0, "Este comando precisa ser executado por um jogador.");
      }

      return var1;
   }

   private static int help(Object var0) {
      RuntimeBridge.sendMessage(var0, "§5§lVELVET ECLIPSE §r§7v0.6.8 Izanagi");
      RuntimeBridge.sendMessage(var0, "§d/velvet status §7- HP/SP, Persona ativa e roster");
      RuntimeBridge.sendMessage(var0, "§d/velvet persona list|owned|add <id>|remove <id>|equip <id> (give continua disponível)");
      RuntimeBridge.sendMessage(var0, "§d/velvet fusion preview|perform <persona1> <persona2> [skill]");
      RuntimeBridge.sendMessage(var0, "§d/velvet diagnostics §7- verifica rede, EntityType, mundo, Shadow e BattleSession");
      RuntimeBridge.sendMessage(var0, "§d/velvet battle start §7- inicia uma batalha imediatamente (fallback do B)");
      RuntimeBridge.sendMessage(var0, "§d/velvet battle nearest §7- inicia batalha contra a Shadow próxima");
      RuntimeBridge.sendMessage(var0, "§d/velvet shadow spawn §7- cria uma Shadow real perto de você");
      RuntimeBridge.sendMessage(var0, "§7Também funciona: §f/summon velvet_eclipse:shadow§7.");
      RuntimeBridge.sendMessage(var0, "§7Na batalha: 1 Attack, 2 Persona, 3 Skill, 4 Item, 5 Guard, 6 Escape.");
      return 1;
   }

   private static int status(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         PlayerPersonaData var2 = PlayerPersonaData.load(var1);
         PersonaInstance var3 = var2.activeInstance();
         PersonaDefinition var4 = PersonaRegistry.persona(var3.definitionId()).orElse(null);
         String var5 = var4 == null ? var3.definitionId() : var4.displayName();
         RuntimeBridge.sendMessage(
            var1,
            "§5[Velvet Eclipse] §fPersona: §d"
               + var5
               + " §7Lv "
               + var3.level()
               + " §8| §cHP "
               + var2.hp()
               + "/"
               + var2.maxHp()
               + " §8| §bSP "
               + var2.sp()
               + "/"
               + var2.maxSp()
               + " §8| §aTonic "
               + var2.pulseTonics()
         );
         RuntimeBridge.sendMessage(var1, "§7Skills: §f" + String.join(", ", var3.learnedSkills()));
         RuntimeBridge.sendMessage(var1, "§7Roster: §f" + var2.describeRoster());
         BattleService.sync(var1, "Status synced");
         return 1;
      }
   }

   private static int restore(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         PlayerPersonaData var2 = PlayerPersonaData.load(var1);
         var2.restore();
         var2.addPulseTonics(3);
         var2.save(var1);
         RuntimeBridge.sendMessage(var1, "§aHP/SP restaurados e +3 Pulse Tonics para testes.");
         BattleService.sync(var1, "Restored");
         return 1;
      }
   }

   private static int listPersonas(Object var0) {
      ArrayList var1 = new ArrayList();
      PersonaRegistry.personas().forEach((var1x, var2) -> var1.add(var1x + "[" + var2.arcana().name() + ",Lv" + var2.baseLevel() + "]"));
      RuntimeBridge.sendMessage(var0, "§dPersonas registradas: §f" + String.join(", ", var1));
      return 1;
   }

   private static int owned(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         RuntimeBridge.sendMessage(var1, "§dRoster: §f" + PlayerPersonaData.load(var1).describeRoster());
         return 1;
      }
   }

   private static int compendium(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         PlayerPersonaData var2 = PlayerPersonaData.load(var1);
         int var3 = var2.rosterIds().size();
         int var4 = PersonaRegistry.personas().size();
         RuntimeBridge.sendMessage(var1, "§5Compendium §f" + var3 + "/" + var4 + " §7(" + var3 * 100 / Math.max(1, var4) + "%)");
         RuntimeBridge.sendMessage(var1, "§7Descobertas: §f" + String.join(", ", var2.rosterIds()));
         return 1;
      }
   }

   private static int givePersona(Object var0, String var1) {
      Object var2 = player(var0);
      if (var2 == null) {
         return 0;
      } else if (PersonaRegistry.persona(var1).isEmpty()) {
         RuntimeBridge.sendMessage(var2, "§cPersona desconhecida: " + var1);
         return 0;
      } else {
         PlayerPersonaData var3 = PlayerPersonaData.load(var2);
         if (!var3.givePersona(var1)) {
            RuntimeBridge.sendMessage(var2, "§eEssa Persona já está no roster.");
            return 0;
         } else {
            var3.save(var2);
            RuntimeBridge.sendMessage(var2, "§aPersona adicionada: §f" + var1);
            BattleService.sync(var2, "Persona acquired");
            return 1;
         }
      }
   }

   private static int removePersona(Object source,String id) {
      Object p=player(source);if(p==null)return 0;
      if(BattleService.current(p)!=null){RuntimeBridge.sendMessage(p,"§eFinalize a batalha antes de remover uma Persona.");return 0;}
      PlayerPersonaData data=PlayerPersonaData.load(p);
      if(!data.rosterIds().contains(id)){RuntimeBridge.sendMessage(p,"§cVocê não possui essa Persona.");return 0;}
      if(!data.removePersona(id)){RuntimeBridge.sendMessage(p,"§eMantenha pelo menos uma Persona na seleção.");return 0;}
      data.save(p);BattleService.sync(p,"Persona removida: "+id);
      RuntimeBridge.sendMessage(p,"§aPersona removida: §f"+id+"§7. Ativa: "+data.activePersona());return 1;
   }

   private static int equipPersona(Object var0, String var1) {
      Object var2 = player(var0);
      if (var2 == null) {
         return 0;
      } else {
         PlayerPersonaData var3 = PlayerPersonaData.load(var2);
         if (!var3.equipPersona(var1)) {
            RuntimeBridge.sendMessage(var2, "§cVocê não possui essa Persona.");
            return 0;
         } else {
            var3.save(var2);
            BattleService.sync(var2, "Persona equipped");
            RuntimeBridge.sendMessage(var2, "§dPersona equipada: §f" + var1);
            return 1;
         }
      }
   }

   private static int previewFusion(Object var0, String var1, String var2) {
      Object var3 = player(var0);
      if (var3 == null) {
         return 0;
      } else {
         PlayerPersonaData var4 = PlayerPersonaData.load(var3);
         PersonaInstance var5 = var4.instance(var1);
         PersonaInstance var6 = var4.instance(var2);
         if (var5 != null && var6 != null) {
            FusionResult var7 = FusionService.preview(var5, var6).orElse(null);
            if (var7 == null) {
               RuntimeBridge.sendMessage(var3, "§cNão existe resultado de fusão para esse par nesta versão.");
               return 0;
            } else {
               RuntimeBridge.sendMessage(var3, "§5Fusion Preview: §f" + var1 + " + " + var2 + " §d→ §f" + var7.personaId());
               RuntimeBridge.sendMessage(
                  var3, "§7Skills herdáveis: §f" + (var7.inheritableSkills().isEmpty() ? "nenhuma" : String.join(", ", var7.inheritableSkills()))
               );
               return 1;
            }
         } else {
            RuntimeBridge.sendMessage(var3, "§cVocê precisa possuir as duas Personas.");
            return 0;
         }
      }
   }

   private static int performFusion(Object var0, String var1, String var2, String var3) {
      Object var4 = player(var0);
      if (var4 == null) {
         return 0;
      } else if (BattleService.current(var4) != null) {
         RuntimeBridge.sendMessage(var4, "§cNão é possível fundir durante uma batalha.");
         return 0;
      } else {
         PlayerPersonaData var5 = PlayerPersonaData.load(var4);
         PersonaInstance var6 = var5.instance(var1);
         PersonaInstance var7 = var5.instance(var2);
         if (var6 != null && var7 != null) {
            FusionResult var8 = FusionService.preview(var6, var7).orElse(null);
            if (var8 == null) {
               RuntimeBridge.sendMessage(var4, "§cEssa combinação ainda não possui regra de fusão.");
               return 0;
            } else if (var3 != null && !var8.inheritableSkills().contains(var3)) {
               RuntimeBridge.sendMessage(var4, "§cSkill não herdável nessa fusão. Opções: " + String.join(", ", var8.inheritableSkills()));
               return 0;
            } else {
               PersonaDefinition var9 = PersonaRegistry.persona(var8.personaId()).orElseThrow();
               PersonaInstance var10 = new PersonaInstance(var9);
               String var11 = var3;
               if (var3 == null && !var8.inheritableSkills().isEmpty()) {
                  var11 = var8.inheritableSkills().get(0);
               }

               if (var11 != null) {
                  var10.inheritSkill(var11);
               }

               if (!var5.replaceFusion(var1, var2, var10)) {
                  RuntimeBridge.sendMessage(var4, "§cFalha ao atualizar o roster.");
                  return 0;
               } else {
                  var5.save(var4);
                  BattleService.sync(var4, "Fusion complete");
                  RuntimeBridge.sendMessage(
                     var4, "§5FUSION! §f" + var1 + " + " + var2 + " §d→ §f" + var9.displayName() + (var11 == null ? "" : " §7[herdou " + var11 + "]")
                  );
                  return 1;
               }
            }
         } else {
            RuntimeBridge.sendMessage(var4, "§cVocê precisa possuir as duas Personas.");
            return 0;
         }
      }
   }

   private static int diagnostics(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         boolean var2 = RuntimeBridge.worldOf(var1) != null;
         boolean var3 = ShadowRegistry.isRegistered();
         boolean var4 = ModNetwork.isRegistered();
         boolean var5 = BattleService.hasActiveBattle(var1);
         int var6 = ShadowWorldService.shadowCount();
         int var7 = ShadowWorldService.nearbyCount(var1, 32.0);
         RuntimeBridge.sendMessage(var1, "§5§lVELVET DIAGNOSTICS");
         RuntimeBridge.sendMessage(var1, "§7Network: " + (var4 ? "§aOK" : "§cFAIL") + " §8| §7Player world: " + (var2 ? "§aOK" : "§cFAIL"));
         RuntimeBridge.sendMessage(
            var1,
            "§7Shadow EntityType: " + (var3 ? "§aOK §fvelvet_eclipse:shadow" : "§cFAIL") + " §8| §7Tracked shadows: §f" + var6 + " §8| §7nearby: §f" + var7
         );
         RuntimeBridge.sendMessage(var1, "§7BattleSession: " + (var5 ? "§aACTIVE" : "§eINACTIVE"));
         if (var2 && var3 && var4) {
            RuntimeBridge.sendMessage(var1, "§aIntegrações essenciais estão registradas.");
            return 1;
         } else {
            RuntimeBridge.sendMessage(var1, "§cUma integração essencial falhou; envie o latest.log contendo 'Velvet Eclipse'.");
            return 0;
         }
      }
   }

   private static int startBattle(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         try {
            BattleService.startDemo(var1);
            if (!BattleService.hasActiveBattle(var1)) {
               RuntimeBridge.sendMessage(var1, "§c[Velvet Eclipse] O comando executou, mas nenhuma BattleSession ficou ativa.");
               return 0;
            } else {
               RuntimeBridge.sendMessage(var1, "§a[Velvet Eclipse] BattleSession iniciada por comando. A HUD deve aparecer agora.");
               return 1;
            }
         } catch (Throwable var3) {
            RuntimeBridge.sendMessage(var1, "§c[Velvet Eclipse] Falha ao iniciar batalha: " + var3.getClass().getSimpleName() + " - " + var3.getMessage());
            VelvetEclipseMod.LOGGER.error("Velvet Eclipse /velvet battle start failed: " + var3);
            return 0;
         }
      }
   }

   private static int startNearestBattle(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else if (!ShadowWorldService.startNearestBattle(var1)) {
         RuntimeBridge.sendMessage(var1, "§eNenhuma Shadow a até 6 blocos. Use /velvet shadow spawn ou /velvet battle start.");
         return 0;
      } else {
         return 1;
      }
   }

   private static int spawnShadow(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         Object var2 = ShadowWorldService.spawnNear(var1);
         if (var2 == null) {
            RuntimeBridge.sendMessage(var1, "§cFalha ao criar a Shadow. Verifique o log por 'shadow entity type'.");
            return 0;
         } else {
            double[] var3 = RuntimeBridge.entityPosition(var2);
            RuntimeBridge.sendMessage(
               var1,
               String.format(
                  Locale.ROOT, "§5Shadow criada à sua frente em §f%.1f %.1f %.1f§5. §7Aproxime-se dela para iniciar o encontro.", var3[0], var3[1], var3[2]
               )
            );
            return 1;
         }
      }
   }

   private static int countShadows(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         RuntimeBridge.sendMessage(
            var1, "§5Shadows carregadas: §f" + ShadowWorldService.shadowCount() + " §7| próximas (32 blocos): §f" + ShadowWorldService.nearbyCount(var1, 32.0)
         );
         return 1;
      }
   }

   private static int nearestShadow(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         Object var2 = ShadowWorldService.nearest(var1, 64.0);
         if (var2 == null) {
            RuntimeBridge.sendMessage(var1, "§eNenhuma Shadow encontrada em 64 blocos.");
            return 0;
         } else {
            double var3 = Math.sqrt(RuntimeBridge.distanceSquared(var1, var2));
            RuntimeBridge.sendMessage(var1, String.format(Locale.ROOT, "§5Shadow mais próxima: §f%.1f blocos", var3));
            return 1;
         }
      }
   }

   private static int demoBattle(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         BattleService.startDemo(var1);
         return 1;
      }
   }

   private static int attack(Object var0) {
      Object var1 = player(var0);
      return var1 != null && BattleService.actFirstTarget(var1, "") ? 1 : 0;
   }

   private static int skill(Object var0, String var1) {
      Object var2 = player(var0);
      return var2 != null && BattleService.skillFirstTarget(var2, var1) ? 1 : 0;
   }

   private static int battlePersona(Object var0, String var1) {
      Object var2 = player(var0);
      return var2 != null && BattleService.switchPersona(var2, var1) ? 1 : 0;
   }

   private static int guard(Object var0) {
      Object var1 = player(var0);
      return var1 != null && BattleService.act(var1, BattleAction.guard()) ? 1 : 0;
   }

   private static int item(Object var0) {
      Object var1 = player(var0);
      return var1 != null && BattleService.act(var1, BattleAction.item("pulse_tonic")) ? 1 : 0;
   }

   private static int escape(Object var0) {
      Object var1 = player(var0);
      return var1 != null && BattleService.act(var1, BattleAction.escape()) ? 1 : 0;
   }

   private static int stop(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         BattleService.stop(var1);
         return 1;
      }
   }

   private static int sync(Object var0) {
      Object var1 = player(var0);
      if (var1 == null) {
         return 0;
      } else {
         BattleService.sync(var1, "Manual sync");
         return 1;
      }
   }
}
