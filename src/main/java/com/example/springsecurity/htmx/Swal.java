
package com.example.springsecurity.htmx;

import com.example.springsecurity.htmx.HxResponseUtil.SwalIcon;

import lombok.Data;

@Data
public class Swal {
  private String title;
  private String text;
  private SwalIcon icon;

  public static Swal success(String title, String text) {
    Swal swal = new Swal();
    swal.setTitle(title);
    swal.setText(text);
    swal.setIcon(SwalIcon.SUCCESS);
    return swal;
  }

  public static Swal error(String title, String text) {
    Swal swal = new Swal();
    swal.setTitle(title);
    swal.setText(text);
    swal.setIcon(SwalIcon.ERROR);
    return swal;
  }

}
