package com.example.monitortest.ui.history.services;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// Opens a new email with a PDF already attached.
// Java has no built-in way to attach files to an email,
// so this asks Apple Mail (macOS) or Outlook (Windows) to create the email instead.
public final class EmailSharer {

    // How long to wait for the email app to create the email.
    private static final long TIMEOUT_SECONDS = 20;

    // Prevent instances of this utility class.
    private EmailSharer() {
    }


    // Opens a new email with the subject, body and attachment filled in.
    // Returns false when no supported email app is available, so the History Page can fall back.
    public static boolean openWithAttachment(String subject, String body, Path attachment) {

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);

        ProcessBuilder builder;

        if (os.contains("mac")) {
            builder = appleMailCommand(subject, body, attachment);
        } else if (os.contains("windows")) {
            builder = outlookCommand(subject, body, attachment);
        } else {
            // Other systems use the fallback on the History Page.
            return false;
        }

        try {

            // Run the command without showing its output.
            Process process = builder
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start();

            // Give up if the email app takes too long to respond.
            if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                process.destroy();
                return false;
            }

            // An exit value of 0 means the email was created.
            return process.exitValue() == 0;

        } catch (IOException e) {

            // The command could not be run, for example when Outlook is not installed.
            e.printStackTrace();
            return false;

        } catch (InterruptedException e) {

            // Keep the interrupt so the rest of EcoScan can respond to it.
            Thread.currentThread().interrupt();
            return false;
        }
    }


    // Builds an AppleScript command that creates a new email in Apple Mail.
    // The subject, body and file are passed separately so special characters are handled safely.
    private static ProcessBuilder appleMailCommand(String subject, String body, Path attachment) {

        return new ProcessBuilder(
                "osascript",
                "-e", "on run argv",
                "-e", "set theSubject to item 1 of argv",
                "-e", "set theBody to item 2 of argv",
                "-e", "set theFile to (POSIX file (item 3 of argv)) as alias",
                "-e", "tell application \"Mail\"",
                "-e", "set newMessage to make new outgoing message with properties {subject:theSubject, content:theBody & return & return, visible:true}",
                "-e", "tell content of newMessage to make new attachment with properties {file name:theFile} at after last paragraph",
                "-e", "activate",
                "-e", "end tell",
                "-e", "end run",
                subject,
                body,
                attachment.toAbsolutePath().toString()
        );
    }


    // Builds a PowerShell command that creates a new email in Outlook.
    // The subject, body and file are passed as environment variables so special characters are handled safely.
    private static ProcessBuilder outlookCommand(String subject, String body, Path attachment) {

        String script =
                "$ErrorActionPreference = 'Stop'; " +
                "$outlook = New-Object -ComObject Outlook.Application; " +
                "$mail = $outlook.CreateItem(0); " +
                "$mail.Subject = $env:ECOSCAN_SUBJECT; " +
                "$mail.Body = $env:ECOSCAN_BODY; " +
                "$null = $mail.Attachments.Add($env:ECOSCAN_ATTACHMENT); " +
                "$mail.Display()";

        ProcessBuilder builder = new ProcessBuilder( "powershell", "-NoProfile", "-NonInteractive", "-Command", script );

        builder.environment().put("ECOSCAN_SUBJECT", subject);
        builder.environment().put("ECOSCAN_BODY", body);
        builder.environment().put("ECOSCAN_ATTACHMENT", attachment.toAbsolutePath().toString());

        return builder;
    }
}