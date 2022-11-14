package com.wrike.pom_parser;

import java.io.InputStream;

import static com.wrike.pom_parser.utils.XmlUtils.readXml;

/**
 * Author: Daniil Shylko
 * Date: 31.08.2022
 */
public class InputStreamPomParser extends AbstractPomParser {

    public InputStreamPomParser(InputStream inputStream) {
        super(readXml(inputStream));
    }

}
