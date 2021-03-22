//package com.feed_the_beast.mods.ftbteamislands;
//
//import net.minecraft.data.DataGenerator;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.common.data.LanguageProvider;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
//
//@Mod.EventBusSubscriber(modid = TeamIslands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
//public final class DataGeneration {
//
//    @SubscribeEvent
//    public static void gatherData(GatherDataEvent event) {
//        ExistingFileHelper helper = event.getExistingFileHelper();
//        DataGenerator generator = event.getGenerator();
//
//        generator.addProvider(new LanguageGenerator(generator));
//    }
//
//    private static class LanguageGenerator extends LanguageProvider {
//        public LanguageGenerator(DataGenerator generator) {
//            super(generator, TeamIslands.MOD_ID, "en_us");
//        }
//
//        @Override
//        protected void addTranslations() {
//            this.add("generator.ftbteamislands.void", "Void World");
//        }
//    }
//}
