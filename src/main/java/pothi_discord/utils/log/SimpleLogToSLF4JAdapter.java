/*
 * MIT License
 *
 * Copyright (c) 2017 Pascal Pothmann
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package pothi_discord.utils.log;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.utils.SimpleLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * @author sedmelluq
 */
public class SimpleLogToSLF4JAdapter implements SimpleLog.LogListener {
  private static final Logger log = LoggerFactory.getLogger(JDA.class);

  @Override
  public void onLog(SimpleLog log, Level logLevel, Object message) {
    switch (logLevel) {
      case DEBUG:
        log.debug(message.toString());
        break;
      case INFO:
        log.info(message.toString());
        break;
      case WARN:
        log.warn(message.toString());
        break;
      case ERROR:
        log.fatal(message.toString());
        break;
    }
  }

  @Override
  public void onError(SimpleLog simpleLog, Throwable err) {
    log.error("An exception occurred", err);
  }
}
