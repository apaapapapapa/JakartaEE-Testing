package com.example.util;

import org.mockito.Mockito;

import jakarta.faces.context.FacesContext;

public abstract class MockFacesContext extends FacesContext {

    public static FacesContext mock() {
        FacesContext context = Mockito.mock(FacesContext.class);
        setCurrentInstance(context); // FacesContextのメソッド
        return context;
      }
}
