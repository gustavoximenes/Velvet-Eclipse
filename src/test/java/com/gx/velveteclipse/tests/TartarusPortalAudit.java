package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.darkhour.*;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.nio.file.*;
import java.util.*;

/** Native Minecraft collision sweeps, without launching a client or server. */
public final class TartarusPortalAudit {
    private static int checks, passages;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static final Set<String> ROUTES=Set.of("forward","back","exit","summit");

    public static void main(String[] args)throws Exception {
        // Inspect compiled registration/override without bootstrapping the Forge registry.
        var node=new org.objectweb.asm.tree.ClassNode();
        try(var in=TartarusPortalAudit.class.getResourceAsStream("/com/gx/velveteclipse/darkhour/TartarusPortalBlock.class")){new org.objectweb.asm.ClassReader(in).accept(node,0);}
        var constructor=node.methods.stream().filter(m->m.name.equals("<init>")).findFirst().orElseThrow();
        check(Arrays.stream(constructor.instructions.toArray()).noneMatch(i->i instanceof org.objectweb.asm.tree.MethodInsnNode m&&m.name.equals("m_60910_")),"no noCollision property disabling the broad phase");
        var collision=node.methods.stream().filter(m->m.name.equals("m_5939_")).findFirst().orElseThrow();
        check(Arrays.stream(collision.instructions.toArray()).anyMatch(i->i instanceof org.objectweb.asm.tree.FieldInsnNode f&&f.name.equals("BARRIER")&&f.owner.endsWith("/TartarusPortalSafety")),"runtime uses the collider tested below");
        check(Arrays.stream(collision.instructions.toArray()).noneMatch(i->i instanceof org.objectweb.asm.tree.JumpInsnNode),"scenic state cannot disable portal collision");
        long start=10_000;
        check(!TartarusPortalSafety.coolingDown(start,0),"new player has no transfer cooldown");
        check(TartarusPortalSafety.coolingDown(start,start),"arrival starts cooldown");
        check(TartarusPortalSafety.coolingDown(start+1999,start),"last cooldown millisecond");
        check(!TartarusPortalSafety.coolingDown(start+2000,start),"exact expiry accepts next request");
        check(!TartarusPortalSafety.coolingDown(start+2100,start),"other player's expiry is independent");
        check(TartarusPortalSafety.coolingDown(start+2100,start+2000),"another player may still be cooling down");

        for(long seed:new long[]{82,20261109})for(int floor:new int[]{0,1,10,25,50,249,250}) {
            var plan=TartarusInteriorLayout.floor(seed,floor);
            for(String route:ROUTES) {
                var cells=plan.cells().stream().filter(c->route.equals(c.material())).toList();
                if(cells.isEmpty())continue;
                passages++;
                int ox=TartarusInteriorLayout.origin(floor),oz=cells.get(0).z();
                check(cells.stream().allMatch(c->c.z()==oz),"generated portal uses X/Y plane");
                double x=ox+cells.stream().mapToInt(TartarusInteriorLayout.Cell::x).average().orElseThrow()+.5;
                double y=64+cells.stream().mapToInt(TartarusInteriorLayout.Cell::y).min().orElseThrow();
                List<VoxelShape> colliders=cells.stream().map(c->TartarusPortalSafety.BARRIER.m_83216_(ox+c.x(),64+c.y(),c.z())).toList();
                // Ordinary sprint, jump sprint, speed effects and large per-tick movement.
                for(double height:new double[]{0,1.25})for(double lateral:new double[]{-1.9,0,1.9})for(double speed:new double[]{.28,.45,1.2,6})for(int side:new int[]{-1,1}) {
                    AABB box=new AABB(x+lateral-.3,y+.05+height,oz+.5+side*3-.3,x+lateral+.3,y+1.85+height,oz+.5+side*3+.3);
                    for(int tick=0;tick<100;tick++) {
                        double dz=Shapes.m_193135_(Axis.Z,box,colliders,-side*speed);
                        box=box.m_82386_(0,0,dz);
                        check(side<0?box.f_82293_<=oz+7.0/16+1e-7:box.f_82290_>=oz+9.0/16-1e-7,"no escape while cooldown or generation pending: "+floor+" "+route);
                    }
                    AABB stopped=box;
                    // entityInside uses the whole block cell, not only its thin collider.
                    check(cells.stream().anyMatch(c->new AABB(ox+c.x(),64+c.y(),c.z(),ox+c.x()+1,65+c.y(),c.z()+1).m_82381_(stopped)),"stopped body remains in trigger: holding forward retries after expiry");
                    double retreat=Shapes.m_193135_(Axis.Z,box,colliders,side*.45);
                    check(Math.abs(retreat-side*.45)<1e-7,"player can back away while floor loads");
                }
            }
        }
        // Reproduce the former no-collision behavior with exactly the same native sweep.
        var oldBox=new AABB(.2,0,-2,.8,1.8,-1.4);
        double oldMove=Shapes.m_193135_(Axis.Z,oldBox,List.of(Shapes.m_83040_()),6);
        check(oldBox.m_82386_(0,0,oldMove).f_82290_>1,"old portal permits sprint-through during ignored requests");
        double fixedMove=Shapes.m_193135_(Axis.Z,oldBox,List.of(TartarusPortalSafety.BARRIER),6);
        check(oldBox.m_82386_(0,0,fixedMove).f_82293_<=7.0/16+1e-7,"same movement stopped by corrected collider");
        var out=Path.of("build/reports/tartarus-portals");Files.createDirectories(out);
        Files.writeString(out.resolve("validation.json"),"{\"passed\":true,\"offlineOnly\":true,\"checks\":"+checks+",\"generatedPassages\":"+passages+",\"nativeCollision\":true}\n");
        System.out.println("PASS Tartarus portals: "+checks+" checks; "+passages+" generated passages, native swept collision, sprint/jump/speed, both directions, cooldown, pending generation and retrigger clearance. Offline only.");
    }
}
