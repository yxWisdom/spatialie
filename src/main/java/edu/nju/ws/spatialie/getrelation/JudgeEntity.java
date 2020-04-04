package edu.nju.ws.spatialie.getrelation;

import edu.nju.ws.spatialie.annprocess.BratUtil;
import edu.nju.ws.spatialie.data.BratDocumentwithList;
import edu.nju.ws.spatialie.data.BratEntity;

public class JudgeEntity {

    static BratDocumentwithList bratDocument;
    public static void init(BratDocumentwithList bratDocument1){
        bratDocument=bratDocument1;
    }

    public static boolean canbeLandmark_strict(BratEntity e){
        String tag = e.getTag();
        return BratUtil.subType(tag).equals(BratUtil.PLACE);
    }

    public static boolean canbeLandmark(BratEntity e){
        String tag = e.getTag();
        return BratUtil.subType(tag).equals(BratUtil.PLACE)||BratUtil.subType(tag).equals(BratUtil.SPATIAL_ENTITY);
    }

    public static boolean canbeTrajector(BratEntity e){
        String tag = e.getTag();
        if (tag.equals(BratUtil.EVENT)){
            if (FindLINK.hasPOSinEntity("N",bratDocument,e)) return true;
        }
        return BratUtil.subType(tag).equals(BratUtil.PLACE)||BratUtil.subType(tag).equals(BratUtil.SPATIAL_ENTITY);
    }

    public static boolean isEvent(BratEntity e) {
        String tag = e.getTag();
//        if (FindLINK.hasPOSinEntity("N",bratDocument,e)) return false;
        return tag.equals(BratUtil.EVENT)||tag.equals(BratUtil.MOTION);
    }

    public static boolean canbeMover(BratEntity e) {
        String tag = e.getTag();
        if (tag.equals(BratUtil.PATH)){
            for (String path: WordData.getPathList()) {
                if (e.getText().contains(path)) return true;
            }
        }
        return tag.equals(BratUtil.SPATIAL_ENTITY);
    }

    public static boolean canbeMover_NotStrict(BratEntity e) {
        String tag = e.getTag();
        return tag.equals(BratUtil.SPATIAL_ENTITY)||tag.equals(BratUtil.EVENT)||tag.equals(BratUtil.PLACE)||tag.equals(BratUtil.PATH);
    }

    public static boolean canBeTogether(BratEntity start, BratEntity next) {
        if (canbeTrajector(start)&& canbeTrajector(next)) return true;
        if (start.getTag().equals(BratUtil.EVENT)&&next.getTag().equals(BratUtil.EVENT)) return true;
        return false;
    }
}
