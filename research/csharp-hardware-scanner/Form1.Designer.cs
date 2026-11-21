namespace EcoScan.HardwareScanner
{
    partial class MainForm
    {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            btnScan = new Button();
            lblStatus = new Label();
            btnExit = new Button();
            SuspendLayout();
            // 
            // btnScan
            // 
            btnScan.Location = new Point(97, 88);
            btnScan.Name = "btnScan";
            btnScan.Size = new Size(119, 35);
            btnScan.TabIndex = 0;
            btnScan.Text = "Scan PC";
            btnScan.UseVisualStyleBackColor = true;
            btnScan.Click += btnScan_Click;
            // 
            // lblStatus
            // 
            lblStatus.AutoSize = true;
            lblStatus.Location = new Point(117, 52);
            lblStatus.Name = "lblStatus";
            lblStatus.Size = new Size(80, 15);
            lblStatus.TabIndex = 1;
            lblStatus.Text = "Ready to scan";
            // 
            // btnExit
            // 
            btnExit.Location = new Point(241, 149);
            btnExit.Name = "btnExit";
            btnExit.Size = new Size(63, 20);
            btnExit.TabIndex = 2;
            btnExit.Text = "Exit";
            btnExit.UseVisualStyleBackColor = true;
            btnExit.Click += btnExit_Click;
            // 
            // MainForm
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(316, 181);
            Controls.Add(btnExit);
            Controls.Add(lblStatus);
            Controls.Add(btnScan);
            Name = "MainForm";
            Text = "EcoScan — Hardware Scanner";
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Button btnScan;
        private Label lblStatus;
        private Button btnExit;
    }
}
