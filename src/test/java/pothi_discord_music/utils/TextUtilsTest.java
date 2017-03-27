package pothi_discord_music.utils;

import org.junit.Test;
import pothi_discord.utils.TextUtils;

import static org.junit.Assert.*;

/**
 * Created by Pascal Pothmann on 29.01.2017.
 */
public class TextUtilsTest {
    @Test
    public void formatMillis() throws Exception {
        assertEquals("1:01:01", TextUtils.formatMillis(3600000+61000));
    }

}