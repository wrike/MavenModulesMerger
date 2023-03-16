package com.wrike.merger.pom;

import com.wrike.merger.pom.utils.XmlUtils;

import java.io.InputStream;

/**
 * Pom parser from the input stream
 *
 * @author daniil.shylko on 31.08.2022
 */
public class InputStreamPomParser extends AbstractPomParser {

    public InputStreamPomParser(InputStream inputStream) {
        super(XmlUtils.readXml(inputStream));
    }

}
