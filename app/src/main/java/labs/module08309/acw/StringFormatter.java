package labs.module08309.acw;

/**
 * Created by Toby on 11/05/2016.
 */
class StringFormatter {
    public static String FormatTime(long millis){
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
