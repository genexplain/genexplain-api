/**
 * Copyright (C) 2017 geneXplain GmbH, Wolfenbuettel, Germany
 *
 * Author: Philip Stegmaier
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY 
 * OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE 
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.genexplain.api.app;

/**
 * @author pst
 *
 */
@Command(name="test-command", description="Just for testings")
public class TestApplicationCommand implements ApplicationCommand {
    
    private static int instances = 0;
    
    public TestApplicationCommand() {
        instances++;
    }
    
    public static int getInstances() { return instances; }
    
    /* (non-Javadoc)
     * @see com.genexplain.api.app.ApplicationCommand#run(java.lang.String[])
     */
    @Override
    public void run(String[] args) throws Exception {
        // TODO Auto-generated method stub

    }

}
