package com.bitbreeds.webrtc.dtls;

/**
 * Copyright (c) 16/05/16, Jonas Waage
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import javafx.util.Pair;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.tls.*;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Vector;


/**
 *
 */
public class WebrtcDtlsServer
        extends DefaultTlsServer {

    private org.bouncycastle.crypto.tls.Certificate cert;

    Pair<java.security.cert.Certificate,KeyPair> pair;

    private final Logger logger = LoggerFactory.getLogger(WebrtcDtlsServer.class);

    public WebrtcDtlsServer(KeyStoreInfo keyStoreInfo) {
        super();

        cert = DTLSUtils.loadCert(keyStoreInfo.getFilePath(),
                keyStoreInfo.getAlias(),
                keyStoreInfo.getPassword());

        pair = DTLSUtils.getCert(keyStoreInfo.getFilePath(),
                keyStoreInfo.getAlias(),
                keyStoreInfo.getPassword());


    }

    public void notifyAlertRaised(short alertLevel, short alertDescription, String message, Throwable cause) {
        logger.warn("DTLS server raised alert: " + AlertLevel.getText(alertLevel)
                + ", " + AlertDescription.getText(alertDescription));

        logger.warn("Level {} msg {} ",AlertLevel.getText(alertLevel),AlertDescription.getText(alertDescription));

        if (message != null) {
            logger.warn(message);
        }
        if (cause != null) {
            logger.error("Cause ",cause);
        }
    }

    public void notifyAlertReceived(short alertLevel, short alertDescription) {
        logger.warn("DTLS server received alert: " + AlertLevel.getText(alertLevel)
                + ", " + AlertDescription.getText(alertDescription));
    }

    protected int[] getCipherSuites() {
        return Arrays.concatenate(super.getCipherSuites(),
                new int[]
                        {
                                CipherSuite.DRAFT_TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256
                        });
    }

    public CertificateRequest getCertificateRequest() throws IOException {
        short[] certificateTypes = new short[]{ClientCertificateType.rsa_sign,
                ClientCertificateType.dss_sign, ClientCertificateType.ecdsa_sign};

        Vector serverSigAlgs = null;
        if (TlsUtils.isSignatureAlgorithmsExtensionAllowed(serverVersion)) {
            serverSigAlgs = TlsUtils.getDefaultSupportedSignatureAlgorithms();
        }

        Vector<X500Name> certificateAuthorities = new Vector<>();
        certificateAuthorities.addElement(
                cert.getCertificateAt(0).getSubject()
        );

        return new CertificateRequest(certificateTypes, serverSigAlgs, certificateAuthorities);
    }

    public void notifyClientCertificate(org.bouncycastle.crypto.tls.Certificate clientCertificate)
            throws IOException {
        Certificate[] chain = clientCertificate.getCertificateList();
        logger.info("DTLS server received client certificate chain of length " + chain.length);
        for (int i = 0; i != chain.length; i++) {
            Certificate entry = chain[i];
            // TODO Create fingerprint based on certificate signature algorithm digest
            logger.info("fingerprint:SHA-256 {} ( {} )",entry.getSignature().toString(),entry.getSubject());
        }
    }

    protected ProtocolVersion getMaximumVersion() {
        return ProtocolVersion.DTLSv12;
    }

    protected ProtocolVersion getMinimumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    protected TlsEncryptionCredentials getRSAEncryptionCredentials()
            throws IOException {

        return new DefaultTlsEncryptionCredentials(context,
                cert,
                new AsymmetricKeyParameter(true));
    }


    protected TlsSignerCredentials getRSASignerCredentials() throws IOException {

        RSAPrivateCrtKey key = (RSAPrivateCrtKey)(pair.getValue().getPrivate());
        return new DefaultTlsSignerCredentials(context,
                cert,
                new RSAKeyParameters(true,key.getModulus(),key.getPrivateExponent()),
                new SignatureAndHashAlgorithm(HashAlgorithm.sha256,SignatureAlgorithm.rsa));
    }

}